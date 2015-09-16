package com.pitmasteriq.qsmart;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceConfig;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.Interval;
import com.idevicesinc.sweetblue.utils.State;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service
{
    private static final int UPDATE_INTERVAL = 2000;
    private static final int TEMPERATURE_OFFSET = 145;
    private static final int NUMBER_OF_ALARM_BITS = 11;

    private static final String TAG = "service";

    private ServiceListener listener;

    private final IBinder binder = new BluetoothBinder();


    private BleManager bleManager;


    private DeviceManager deviceManager;


    private ExceptionManager exceptionManager;


    private PowerManager powerManager;


    private UpdateThread updateThread;

    private ConnectionTransaction connectionTransaction;


    private boolean isScanning = false;
    private int scanTime = 500;
    private int scanInterval = 2000;
    private boolean updateScanTime = false;

    private boolean connectionAttemptActive = false;


    private Handler handler = new Handler();


    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;


    public BluetoothService()
    {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        bleManager = BleManager.get(getApplicationContext());


        deviceManager = DeviceManager.get(getApplicationContext());


        exceptionManager = ExceptionManager.get(getApplicationContext());


        updateThread = new UpdateThread(handler, new UpdateRunnable(), UPDATE_INTERVAL);


        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);


        prefs = getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {


        if(!updateThread.isAlive())
            updateThread.start();

        Log.i(TAG, "service started");

        startForeground(1,Notifications.getServiceNotification(getApplicationContext()));

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        stopForeground(true);

        updateThread.setRunning(false);

        stopScanning();

        Log.i(TAG, "service stopped");
    }



    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }



    public class BluetoothBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }



    public void updateConfigurationValue(int selector, int value)
    {
        if(selector == -1 || value == -1 || prefs.getString(Preferences.CONNECTED_ADDRESS, null) == null)
        {
            Log.i(TAG, "config change failed " + selector + " " + value + " " + prefs.getString(Preferences.CONNECTED_ADDRESS, null));
            listener.configurationChangeFailed();
            return;
        }

        Log.i(TAG,"Writing Configuration Change.");
        writeConfigChange(selector, value);
    }








    private void writeConfigChange(int configSelector, int value)
    {
        String address = prefs.getString(Preferences.CONNECTED_ADDRESS, null);

        if(address == null)
        {
            listener.configurationChangeFailed();
            return;
        }

        Log.i( TAG, "Attempting to write configuration for " + address);

        BluetoothGattCharacteristic c = null;
        byte[] data = null;

        BleDevice device = bleManager.getDevice( address );
        Log.i(TAG, "Config: Updating " + address);

        if( device.is( BleDeviceState.INITIALIZED) )
        {
            c = device.getNativeCharacteristic(Uuid.CONFIG_BASIC);
            if( c != null )
            {
                Log.i(TAG, "Writing Configuration Change.");

                short sValue = (short) value;

                if(configSelector == DeviceConfig.CONFIG_PIT_SET || configSelector == DeviceConfig.CONFIG_DELAY_PIT_SET ||
                        configSelector == DeviceConfig.CONFIG_FOOD_1_PIT_SET || configSelector == DeviceConfig.CONFIG_FOOD_2_PIT_SET )
                    if (sValue != 0)
                        sValue -= TEMPERATURE_OFFSET;

                ByteBuffer bytes = ByteBuffer.allocate(2).putShort(sValue);
                byte[] array = bytes.array();

                data = new byte[3];
                data[0] = (byte)configSelector;
                data[1] = array[0];
                data[2] = array[1];

                Log.d( TAG, "DATA: " + data[0] + " | " + data[1] + " | " + data[2] );

                device.write(Uuid.CONFIG_BASIC, data, new BleDevice.ReadWriteListener()
                {
                    @Override
                    public void onEvent(ReadWriteEvent e)
                    {
                        if( e.status() == BleDevice.ReadWriteListener.Status.SUCCESS )
                        {
                            Log.i(TAG, "wrote Configuration Change.");
                            listener.configurationChangeSucceeded();
                        }
                        else
                        {
                            Log.i(TAG, "failed Configuration Change.");
                            listener.configurationChangeFailed();
                        }
                    }
                });
            }
        }
    }


    public void connectToDevice(String address)
    {
        //if (!connectionAttemptActive)
        //{
            connectionAttemptActive = true;



            for (BleDevice d : bleManager.getDevices_List())
            {
                if (d.getMacAddress().equals(address))
                {
                    d.setConfig(new BleDeviceConfig()
                    {{
                            //TODO Uncomment to turn off auto reconnect feature
                            this.reconnectRequestFilter_longTerm = new DefaultReconnectRequestFilter(Interval.DISABLED);
                            this.reconnectRequestFilter_shortTerm = new DefaultReconnectRequestFilter(Interval.DISABLED);
                            this.reconnectPersistFilter_longTerm = new DefaultReconnectPersistFilter(Interval.DISABLED);
                            this.reconnectPersistFilter_shortTerm = new DefaultReconnectPersistFilter(Interval.DISABLED);
                        }});


                    Log.w(TAG, "Starting connection");
                    d.connect(new ConnectionListener(), new ConnectionFailListener());
                    return;
                }
            }
            connectionAttemptActive = false;
            listener.newDeviceFailed(address);
        //}
        //else
        //{
        //    Toast.makeText(this,"Connection attempt already started. Please Wait...",Toast.LENGTH_SHORT).show();
        //}
    }


    public void disconnectFromDevice()
    {
        String address = prefs.getString(Preferences.CONNECTED_ADDRESS, null);
        boolean found = false;

        if (address == null)
        {
            listener.disconnectFailed();
            return;
        }

        for(BleDevice d : bleManager.getDevices_List())
        {
            if(d.getMacAddress().equals(address))
            {
                d.disconnect();
                found = true;
            }
        }

        if(!found)
            listener.disconnectFailed();
    }

    public void cancelConnectionAttempt()
    {
        connectionTransaction.cancel();
    }

    public BleManager.DiscoveryListener discoveryListener = new BleManager.DiscoveryListener()
    {
        @Override
        public void onEvent(DiscoveryEvent e)
        {
            boolean serviceFound = false;
            for (UUID uuid : e.device().getAdvertisedServices())
            {
                if(uuid.equals(Uuid.SERVICE))
                    serviceFound = true;
            }

            if(!serviceFound)
                return;


            if (e.lifeCycle() == LifeCycle.DISCOVERED)
            {
                ScannedDevices.get().addressDiscovered(e.device().getMacAddress());
                Log.e("Discovered", e.device().getMacAddress() + ":" + e.device().getName_native());
            }

            if (e.lifeCycle() == LifeCycle.REDISCOVERED)
                ScannedDevices.get().addressRediscovered(e.device().getMacAddress());

            if (e.lifeCycle() == LifeCycle.UNDISCOVERED)
                ScannedDevices.get().addressUndiscovered(e.device().getMacAddress());



            if (prefs.getString(Preferences.RECONNECT_ADDRESS, null) != null)
            {
                if (e.device().getMacAddress().equals(prefs.getString(Preferences.RECONNECT_ADDRESS, null)))
                {
                    Log.w(TAG, "Attempting to reconnect to " + e.device().getMacAddress());
                    deviceManager.newDevice(e.device().getMacAddress());
                    connectToDevice(e.device().getMacAddress());
                }
            }
        }
    };

    /**
     * Stops the bluetooth radio from scanning
     */
    private void stopScanning()
    {
        bleManager.stopPeriodicScan();

        isScanning = false;

        Log.i(TAG, "scanning stopped");
    }






    public void setServiceListener(ServiceListener listener)
    {
        this.listener = listener;
    }




    private boolean isAppRunning()
    {
        if( prefs.getBoolean(Preferences.HAS_FOREGROUND, false))
        {
            if(prevAppState == false)
                appStateChanged = true;

            prevAppState = true;
            return true;
        }

        if(prevAppState)
            appStateChanged = true;

        prevAppState = false;
        return false;
    }

    private boolean appStateChanged = false;
    private boolean prevAppState = false;       //true for running, false for in background

    private class UpdateRunnable implements Runnable
    {
        @Override
        public void run()
        {
            if(deviceManager.device() != null)
            {
                if (deviceManager.device().exceptions().hasAlarm())
                {
                    exceptionManager.startAlarm();
                    exceptionManager.sendExceptionNotification();
                }
                else if (deviceManager.device().exceptions().hasNotify())
                {
                    exceptionManager.sendExceptionNotification();
                }
                else
                {
                    if(exceptionManager.isAlarmSounding())
                        exceptionManager.stopAlarm();

                    if(exceptionManager.isNotificationActive())
                        exceptionManager.cancelNotification(ExceptionManager.ALARM);
                }
            }



            //check for disconnect error
            if(prefs != null)
            {
                if (prefs.getString(Preferences.RECONNECT_ADDRESS, null) != null)
                {
                    long lostTime = prefs.getLong(Preferences.CONNECTION_LOST_TIME, -1);

                    if (lostTime == -1)
                        return;

                    if (System.currentTimeMillis() - lostTime > (10000))
                        if (deviceManager.device() != null)
                            deviceManager.device().exceptions().addException(DeviceExceptions.Exception.CONNECTION_LOST);
                }
            }
        }
    }





    public interface ServiceListener
    {
        void onServiceReady();
        void onServiceStopped();

        void connectionFailed(String msg, String address);

        void newDeviceAdded(String address);
        void newDeviceFailed(String address);
        void newDeviceTimeout(String address);

        void configurationChangeFailed();
        void configurationChangeSucceeded();

        void disconnectFailed();
        void disconnectSucceeded();
    }










    private void startTransaction(BleDevice d)
    {
        connectionTransaction = new ConnectionTransaction(getApplicationContext());
        connectionTransaction.setConnectionListener(connectionResponseListener);
        connectionTransaction.start(d);
    }



    private ConnectionTransaction.ConnectionListener connectionResponseListener = new ConnectionTransaction.ConnectionListener()
    {
        @Override
        public void onDataReceived(BleDevice device, byte[] data)
        {
            //TODO parse data
            new BackgroundThread(new ParseDataRunnable(device, data)).start();
        }

        @Override
        public void onPasscodeAccepted(BleDevice device)
        {
            connectionAttemptActive = false;

            //set internal preferences to reflect connection status
            editor.putString(Preferences.CONNECTED_ADDRESS, device.getMacAddress())
                    .putString(Preferences.RECONNECT_ADDRESS, null).commit();

            if( deviceManager.device() != null)
            {
                //remove connection lost flag from device
                deviceManager.device().exceptions().removeException(DeviceExceptions.Exception.CONNECTION_LOST);

                //update device status
                deviceManager.device().setStatus(Device.Status.OK);
            }

            //notify the activity that we connected
            listener.newDeviceAdded(device.getMacAddress());
        }

        @Override
        public void onPasscodeFailed(BleDevice device)
        {
            connectionAttemptActive = false;

            if(prefs.getString(Preferences.RECONNECT_ADDRESS, null) == null)
            {
                //passcode failed on initial connection
                deviceManager.clear();
                listener.newDeviceFailed(device.getMacAddress());
            }
            else
            {
                //passcode failed on reconnect attempt
                editor.putString(Preferences.RECONNECT_ADDRESS, null).commit();
                deviceManager.device().exceptions().addException(DeviceExceptions.Exception.CONNECTION_LOST);
            }

        }

        @Override
        public void onPasscodeTimeout(BleDevice device)
        {
            connectionAttemptActive = false;

            if(prefs.getString(Preferences.RECONNECT_ADDRESS, null) == null)
            {
                //passcode timed out on initial connection
                listener.newDeviceTimeout(device.getMacAddress());
                deviceManager.clear();
            }
            else
            {
                try
                {
                    //passcode timed out on a reconnect attempt
                    editor.putString(Preferences.RECONNECT_ADDRESS, null).commit();
                    deviceManager.device().exceptions().addException(DeviceExceptions.Exception.CONNECTION_LOST);
                }
                catch(NullPointerException e)
                {
                    //Something bad happened
                    deviceManager.clear();
                }
            }
        }
    };


    private class ConnectionListener implements BleDevice.StateListener
    {
        @Override
        public void onEvent(StateEvent e)
        {
            if(e.didEnter(BleDeviceState.CONNECTED))
            {
                Log.i(TAG, "connected to " + e.device().getMacAddress());
            }

            if(e.didEnter(BleDeviceState.INITIALIZED))
            {
                Log.i(TAG, "initialized to " + e.device().getMacAddress());

                if(e.device().is(BleDeviceState.BONDED))
                {
                    Log.i(TAG, "already bonded to " + e.device().getMacAddress());
                    startTransaction(e.device());
                }
                else
                {
                    e.device().bond(new BleDevice.BondListener()
                    {
                        @Override
                        public void onEvent(BondEvent e)
                        {
                            if(e.wasSuccess())
                            {
                                Log.i(TAG, "bonded to " + e.device().getMacAddress());
                                startTransaction(e.device());
                            }
                        }
                    });
                }
            }

            if(e.didEnter(BleDeviceState.DISCONNECTED))
            {
                connectionAttemptActive = false;

                e.device().unbond();

                editor.putString(Preferences.CONNECTED_ADDRESS, null).commit();

                State.ChangeIntent state = e.device().getLastDisconnectIntent();

                if(state == State.ChangeIntent.INTENTIONAL)
                {
                    Log.i(TAG, "Intentional disconnect from " + e.device().getMacAddress());

                    listener.disconnectSucceeded();

                    if (deviceManager.device() != null)
                        deviceManager.device().setStatus(Device.Status.Disconnected);
                }
                else
                {
                    Log.i(TAG, "Unintentional disconnect from " + e.device().getMacAddress());

                    if (deviceManager.device() != null)
                        deviceManager.device().setStatus(Device.Status.LostConnection);

                    editor.putString(Preferences.RECONNECT_ADDRESS, e.device().getMacAddress())
                            .putLong(Preferences.CONNECTION_LOST_TIME, System.currentTimeMillis()).commit();
                }
            }
        }
    }

    private class ConnectionFailListener implements BleDevice.ConnectionFailListener
    {
        @Override
        public Please onEvent(ConnectionFailEvent event)
        {
            connectionAttemptActive = false;
            listener.connectionFailed(event.status().toString(), event.device().getMacAddress());
            editor.putString(Preferences.CONNECTED_ADDRESS, null).commit();
            deviceManager.clear();

            Log.e(TAG, "Connection Failed: " + event.device().getMacAddress() + " : " + event.status());
            return null;
        }
    }








    private class ParseDataRunnable implements Runnable
    {
        private BleDevice device;
        private byte[] data;

        public ParseDataRunnable(BleDevice device, byte[] data)
        {
            this.device = device;
            this.data = data;
        }

        @Override
        public void run()
        {
            if (data.length == 20)
            {
                short value = 0;
                List<Short> values = new ArrayList<>();

                values.add((short) data[0]);
                values.add((short) data[1]);

                value = bytesToShort((byte) 0, data[2]);
                values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

                values.add(bytesToShort(data[3], data[4]));
                values.add(bytesToShort(data[5], data[6]));
                values.add(bytesToShort(data[7], data[8]));

                value = bytesToShort((byte) 0, data[10]);
                values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

                values.add(bytesToShort((byte) 0, data[11]));
                values.add(bytesToShort((byte) 0, data[12]));
                values.add((short) data[13]);
                values.add(bytesToShort((byte) 0, data[16]));

                value = bytesToShort((byte) 0, data[17]);
                values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

                values.add(bytesToShort((byte) 0, data[18]));

                value = bytesToShort((byte) 0, data[19]);
                values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

                values.add(getHighBits((short)data[9]));

                //convert two byte flag bit fields into short

                short flagBits = bytesToShort(data[14], data[15]);
                //convert short into boolean array by converting each bit in the
                //short from a 1 to true and 0 to false
                boolean bits[] = new boolean[NUMBER_OF_ALARM_BITS];

                String flagHash = "";
                for (int i = NUMBER_OF_ALARM_BITS - 1; i >= 0; i--)
                {
                    flagHash += (flagBits & (1 << i));
                    bits[i] = (flagBits & (1 << i)) != 0;
                }



                if(deviceManager.device() != null)
                {
                    //TODO If flag bits have changed, do something
                    if (deviceManager.device().exceptions().compareHash(flagHash) == false)
                        handleFlagBits(bits);

                    if (values.get(3) == 999)
                        deviceManager.device().exceptions().addException(DeviceExceptions.Exception.PIT_PROBE_ERROR);
                    else
                        deviceManager.device().exceptions().removeException(DeviceExceptions.Exception.PIT_PROBE_ERROR);
                }

                //UPDATE DEVICE VALUES
                deviceManager.updateValues(values);

                //if screen is off, save to file.
                if (!isAppRunning())
                {
                    deviceManager.saveDevice();
                }
            }
        }

        private void handleFlagBits(boolean[] bits)
        {
            String flags = "";
            for (int i = 0; i < bits.length; i++)
            {
                if (bits[i])
                {
                    flags += "1 | ";

                    deviceManager.device().exceptions().addException(i);
                }
                else // bit is false
                {
                    flags += "0 | ";

                    deviceManager.device().exceptions().removeException(i);
                }
            }

            Log.e(TAG, flags);
        }

        private short getHighBits(short value)
        {
            int high = (value & 0XF0) >> 4;
            return (short) high;
        }

        private short bytesToShort(byte i, byte j)
        {
            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.put(j);
            bb.put(i);
            return bb.getShort(0);
        }
    }


}
