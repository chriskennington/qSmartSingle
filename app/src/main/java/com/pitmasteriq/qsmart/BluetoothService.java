package com.pitmasteriq.qsmart;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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

public class BluetoothService extends Service
{
    public static final int ALARM_WAIT_TIME = 60000;


    private static final int UPDATE_INTERVAL = 1000;
    private static final int TEMPERATURE_OFFSET = 145;
    private static final int NUMBER_OF_ALARM_BITS = 11;

    private ServiceListener listener;
    private final IBinder binder = new BluetoothBinder();

    private BleManager bleManager;

    private BleDevice connectingDevice; //device currently attempting connection
    private BleDevice connectedDevice; //currently connected device
    private BleDevice reconnectDevice; //device to reconnect to when back in range

    private DeviceManager deviceManager;
    private ExceptionManager exceptionManager;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Handler handler = new Handler();
    private UpdateThread updateThread;

    private DataSource dataSource;
    private SaveDataThread saveThread;

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

        //access to database
        dataSource = new DataSource(getApplicationContext());

        prefs = getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        editor = prefs.edit();

        updateThread = new UpdateThread(handler, new UpdateRunnable(), UPDATE_INTERVAL);
        saveThread = new SaveDataThread();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(!updateThread.isAlive())
            updateThread.start();

        startForeground(1, exceptionManager.getServiceNotification());

        if (!saveThread.isAlive())
            saveThread.start();

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();

        updateThread.setRunning(false);
        saveThread.setRunning(false);

        handler.removeCallbacksAndMessages(null);

        stopForeground(true);
    }

    public class BluetoothBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }

    public interface ServiceListener
    {
        void passcodeAccepted();
        void passcodeDeclined();

        void connectSucceeded();
        void connectFailed();

        void disconnectSucceeded();
        void disconnectFailed();

        void configChangeSucceeded();
        void configChangeFailed();
    }

    public void setServiceListener(ServiceListener listener)
    {
        this.listener = listener;
    }

    public BleManager.DiscoveryListener discoveryListener = new BleManager.DiscoveryListener()
    {
        //regex for IQ Name "[I][Q]\d{4}"

        @Override
        public void onEvent(DiscoveryEvent e)
        {
            //if name does not match expression assume it is not an IQ and return
            if (!e.device().getName_native().matches("[I][Q]\\d{4}"))
                return;

            if (e.was(LifeCycle.DISCOVERED))
            {
                ScannedDevices.get().addressDiscovered(e.device().getMacAddress());
                Log.e("Discovered", e.device().getMacAddress() + ":" + e.device().getName_native());
            }

            if (e.was(LifeCycle.REDISCOVERED))
            {
                ScannedDevices.get().addressRediscovered(e.device().getMacAddress());
            }

            if (e.was(LifeCycle.UNDISCOVERED))
            {
                ScannedDevices.get().addressUndiscovered(e.device().getMacAddress());
            }

            //reconnect to lost device
            if (reconnectDevice != null)
            {
                if (reconnectDevice.getMacAddress().equals(e.device().getMacAddress()))
                    startConnection(reconnectDevice);
            }
        }
    };

    public void disconnectFromDevice()
    {
        if(connectedDevice == null)
        {
            listener.disconnectFailed();
            return;
        }

        connectedDevice.disconnect();
    }

    public void connectToAddress(String address)
    {
        if (connectedDevice != null)
            disconnectFromDevice();

        for (BleDevice d : bleManager.getDevices_List())
            if (d.getMacAddress().equals(address))
            {
                connectingDevice = d;
                startConnection();
            }
    }

    public void cancelConnectionAttempt()
    {

    }

    public void updateConfigurationValue(int selector, int value)
    {
        if(selector == -1 || value == -1 || connectedDevice == null)
        {
            listener.configChangeFailed();
            return;
        }

        writeConfigChange(selector, value);
    }

    private void writeConfigChange(int selector, int value)
    {
        //TODO
        if (connectedDevice == null)
        {
            listener.configChangeFailed();
            return;
        }

        BluetoothGattCharacteristic c = connectedDevice.getNativeCharacteristic(Uuid.CONFIG_BASIC);

        if (c != null)
        {
            short sValue = (short) value;

            //subtract TEMPERATURE_OFFSET from appropriate values
            if ( (selector == DeviceConfig.CONFIG_PIT_SET
                    || selector == DeviceConfig.CONFIG_DELAY_PIT_SET
                    || selector == DeviceConfig.CONFIG_FOOD_1_PIT_SET
                    || selector == DeviceConfig.CONFIG_FOOD_2_PIT_SET)
                    && sValue != 0)
            {
                sValue -= TEMPERATURE_OFFSET;
            }

            ByteBuffer bytes = ByteBuffer.allocate(2).putShort(sValue);
            byte[] array = bytes.array();

            byte[] data = new byte[3];
            data[0] = (byte) selector;
            data[1] = array[0];
            data[2] = array[1];

            connectedDevice.write(Uuid.CONFIG_BASIC, data, new BleDevice.ReadWriteListener()
            {
                @Override
                public void onEvent(ReadWriteEvent e)
                {
                    if (e.status() == BleDevice.ReadWriteListener.Status.SUCCESS)
                    {
                        Log.i("TAG", "wrote Configuration Change.");
                        listener.configChangeSucceeded();
                    } else
                    {
                        Log.i("TAG", "failed Configuration Change.");
                        listener.configChangeFailed();
                    }
                }
            });
        }
    }

    private void startConnection()
    {

        connectingDevice.setConfig(new BleDeviceConfig()
        {{
                //TODO Uncomment to turn off auto reconnect feature
                this.reconnectRequestFilter_longTerm = new DefaultReconnectRequestFilter(Interval.DISABLED);
                this.reconnectRequestFilter_shortTerm = new DefaultReconnectRequestFilter(Interval.DISABLED);
                this.reconnectPersistFilter_longTerm = new DefaultReconnectPersistFilter(Interval.DISABLED);
                this.reconnectPersistFilter_shortTerm = new DefaultReconnectPersistFilter(Interval.DISABLED);
            }});

        connectingDevice.connect(new ConnectionStateListener());
    }

    private void startConnection(BleDevice d)
    {
        connectingDevice = d;
        startConnection();
    }

    private void writePasscode()
    {
        Log.i("TAG", "sending passcode");
        short value = Short.parseShort(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(Preferences.PASSCODE, "0000"));

        if (value == 0)
        {
            //TODO passcode set to 0 send message to user

            Log.i("TAG", "passcode 0, quitting");
            connectingDevice.disconnect();
            connectingDevice = null;
            return;
        }

        byte[] data = ByteBuffer.allocate(2).putShort( value ).array();

        connectingDevice.write(Uuid.PASSCODE, data, new BleDevice.ReadWriteListener()
        {
            @Override
            public void onEvent(BleDevice.ReadWriteListener.ReadWriteEvent e)
            {
                if (e.wasSuccess())
                {
                    new BackgroundThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // sleep for 3 seconds before checking passcode response
                            Log.i("TAG", "sleeping for 3 seconds");
                            try
                            {
                                Thread.sleep(3000);
                            } catch (InterruptedException e)
                            {
                            }

                            Log.i("TAG", "checking passcode validity");
                            checkPasscodeValidity();
                        }
                    }).start();
                } else
                {
                    Log.i("TAG", "write failed?");
                }
            }
        });
    }

    private void checkPasscodeValidity()
    {
        connectingDevice.read(Uuid.STATUS_BASIC, new BleDevice.ReadWriteListener()
        {
            @Override
            public void onEvent(ReadWriteEvent e)
            {
                for (int i = 0; i < 20; i++)
                    Log.e("TAG", "" + e.data()[i]);

                if (e.data()[19] == 1) //bad passcode response
                {
                    badPasscodeResponse();
                } else if (e.data()[19] == 0) //good passcode response
                {
                    goodPasscodeResponse();
                }
            }
        });
    }

    private void badPasscodeResponse()
    {
        connectingDevice = null;

        //send message to activity
        listener.passcodeDeclined();

        Log.i("TAG", "passcode declined");
    }

    private void goodPasscodeResponse()
    {
        Log.i("TAG", "passcode accepted");

        connectedDevice = connectingDevice;
        connectingDevice = null;
        reconnectDevice = null;

        deviceManager.newDevice(connectedDevice.getMacAddress());
        connectedDevice.enableNotify(Uuid.STATUS_BASIC, new DataListener());
        deviceManager.device().setStatus(Device.Status.OK);

        //remove connection lost flag from device
        deviceManager.device().exceptions().removeException(DeviceExceptions.Exception.CONNECTION_LOST);

        //send message to activity
        listener.passcodeAccepted();
    }

    private void setAlarm()
    {
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getActivity(this, 0, intent, 0);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), alarmIntent);
    }

    private class ConnectionStateListener implements BleDevice.StateListener
    {
        @Override
        public void onEvent(StateEvent e)
        {
            if (e.didEnter(BleDeviceState.INITIALIZED))
            {
                Log.i("TAG", "initialized");

                if (e.device().is(BleDeviceState.BONDED))
                    writePasscode();
                else
                    e.device().bond(new BleDevice.BondListener()
                    {
                        @Override
                        public void onEvent(BondEvent e)
                        {
                            if(e.wasSuccess())
                            {
                                Log.i("TAG", "bonded to " + e.device().getMacAddress());
                                writePasscode();
                            }
                        }
                    });
            }

            if (e.didEnter(BleDeviceState.DISCONNECTED))
            {
                Log.i("TAG", "disconnected");
                State.ChangeIntent state = e.device().getLastDisconnectIntent();

                if(state == State.ChangeIntent.INTENTIONAL)
                {
                    listener.disconnectSucceeded();
                    connectedDevice = null;
                    deviceManager.device().setStatus(Device.Status.Disconnected);
                }
                else
                {
                    reconnectDevice = connectedDevice;
                    connectedDevice = null;

                    editor.putLong(Preferences.CONNECTION_LOST_TIME, System.currentTimeMillis()).commit();

                    deviceManager.device().setStatus(Device.Status.LostConnection);
                }
            }
        }
    }

    private class DataListener implements BleDevice.ReadWriteListener
    {
        @Override
        public void onEvent(ReadWriteEvent e)
        {
            if (e.data().length == 20)
            {
                editor.putLong(Preferences.LAST_UPDATE_TIME, System.currentTimeMillis());
                new BackgroundThread(new ParseDataRunnable(e.device(), e.data())).start();
            }
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
                List<Short> values

                        = new ArrayList<>();

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
                if (!deviceManager.updateValues(values))
                {
                    //TODO does something need to be done here?
                    //for some reason the device is null.
                }

                //if screen is off, save to file.
                if (!MyApplication.isActivityVisible())
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

            //Log.e("TAG", flags);
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

    private class SaveDataThread extends Thread
    {
        private boolean isRunning = true;
        @Override
        public void run()
        {
            while (isRunning)
            {
                if (deviceManager.device() != null)
                {
                    Log.e("TAG", "saving data");
                    dataSource.open();
                    dataSource.storeDataString();
                    dataSource.close();
                }

                try
                {
                    sleep(60000);
                } catch (InterruptedException e){}
            }
        }

        public void setRunning(boolean running){isRunning = running;}
    }

    private class UpdateRunnable implements Runnable
    {
        @Override
        public void run()
        {
            if(deviceManager.device() != null)
            {
                if (deviceManager.device().exceptions().hasAlarm())
                {
                    if (exceptionManager.canStartAlarm())
                        setAlarm();
                }
                else if (deviceManager.device().exceptions().hasNotify())
                {
                    exceptionManager.sendExceptionNotification();
                }
            }

            //check if we have not received an update in the last 5 minutes
            long lastUpdate = prefs.getLong(Preferences.LAST_UPDATE_TIME, -1);
            if (lastUpdate != -1 && (System.currentTimeMillis() - lastUpdate) > 300000)
            {
                if (deviceManager.device() != null)
                    deviceManager.device().exceptions().addException(DeviceExceptions.Exception.CONNECTION_LOST);
            }

            //check for disconnect error
            if (reconnectDevice != null)
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
