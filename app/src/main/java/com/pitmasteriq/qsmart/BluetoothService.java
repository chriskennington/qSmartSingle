package com.pitmasteriq.qsmart;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceConfig;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.Interval;
import com.idevicesinc.sweetblue.utils.State;

import java.nio.ByteBuffer;

public class BluetoothService extends Service
{

    private ServiceListener listener;
    private final IBinder binder = new BluetoothBinder();

    private BleManager bleManager;

    private BleDevice connectingDevice; //device currently attempting connection
    private BleDevice connectedDevice; //currently connected device
    private BleDevice reconnectDevice; //device to reconnect to when back in range

    public BluetoothService()
    {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        bleManager = BleManager.get(getApplicationContext());

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
        void passcodeDeclined();
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

    public void connectToAddress(String address)
    {
        for (BleDevice d : bleManager.getDevices_List())
            if (d.getMacAddress().equals(address))
            {
                connectingDevice = d;
                startConnection();
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
                Log.i("TAG", "test");
                if (e.wasSuccess())
                {
                    Log.i("TAG", "test");
                    new BackgroundThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // sleep for 3 seconds before checking passcode response
                            Log.i("TAG", "sleeping for 3 seconds");
                            try{Thread.sleep(3000);} catch(InterruptedException e){}

                            Log.i("TAG", "checking passcode validity");
                            checkPasscodeValidity();
                        }
                    }).start();
                }
                else
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
                for (int i=0; i<20; i++)
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
        Log.i("TAG", "passcode declined");
    }

    private void goodPasscodeResponse()
    {
        Log.i("TAG", "passcode accepted");
        connectedDevice = connectingDevice;
        connectingDevice = null;
        reconnectDevice = null;

        connectedDevice.enableNotify(Uuid.STATUS_BASIC, new DataListener());

        //TODO send message to user
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
                    e.device().bond();
            }

            if (e.didEnter(BleDeviceState.BONDED))
            {
                Log.i("TAG", "bonded");
                writePasscode();
            }

            if (e.didEnter(BleDeviceState.DISCONNECTED))
            {
                Log.i("TAG", "disconnected");


                State.ChangeIntent state = e.device().getLastDisconnectIntent();

                if(state == State.ChangeIntent.INTENTIONAL)
                {
                    connectedDevice = null;
                }
                else
                {
                    reconnectDevice = connectedDevice;
                    connectedDevice = null;
                }
            }
        }
    }

    private class DataListener implements BleDevice.ReadWriteListener
    {
        @Override
        public void onEvent(ReadWriteEvent e)
        {
            Log.i("TAG", "Data updated");
        }
    }
}
