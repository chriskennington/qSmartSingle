package com.pitmasteriq.qsmart.service;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.State;
import com.pitmasteriq.qsmart.BackgroundThread;
import com.pitmasteriq.qsmart.ConnectionHandler;
import com.pitmasteriq.qsmart.Console;
import com.pitmasteriq.qsmart.DeviceConfig;
import com.pitmasteriq.qsmart.DeviceManager;
import com.pitmasteriq.qsmart.Preferences;
import com.pitmasteriq.qsmart.Uuid;
import com.pitmasteriq.qsmart.exception.*;
import com.pitmasteriq.qsmart.notifications.NotificationHelper;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class BluetoothService extends Service
{
    public static final String ACTION_CONNECT = "com.pitmaster.qsmart.ACTION_CONNECT";
    public static final String ACTION_DISCONNECT = "com.pitmaster.qsmart.ACTION_DISCONNECT";
    public static final String ACTION_CANCEL_CONNECTION = "com.pitmaster.qsmart.ACTION_CANCEL_CONNECTION";
    public static final String ACTION_CONFIG_CHANGE = "com.pitmaster.qsmart.CONFIG_CHANGE";

    public static final String DISCONNECTED_SUCCESSFULLY = "com.pitmaster.qsmart.DISCONNECTED_SUCCESSFULLY";
    public static final String EXCEPTIONS_UPDATED = "com.pitmaster.qsmart.EXCEPTIONS_UPDATED";
    public static final String PASSCODE_ACCEPTED = "com.pitmaster.qsmart.PASSCODE_ACCEPTED";
    public static final String PASSCODE_DECLINED = "com.pitmaster.qsmart.PASSCODE_DECLINED";
    public static final String CONFIG_CHANGE_GOOD = "com.pitmaster.qsmart.CONFIG_CHANGE_GOOD";
    public static final String CONFIG_CHANGE_BAD = "com.pitmaster.qsmart.CONFIG_CHANGE_BAD";
    public static final String PASSCODE_NULL = "com.pitmaster.qsmart.PASSCODE_NULL";

    private static final int MSG_CONNECT = 0;
    private static final int MSG_DISCONNECT = 1;
    private static final int MSG_WRITE_PASSCODE = 2;

    private static final int TEMPERATURE_OFFSET = 145;
    private static final int SYSTEM_CHECK_INTERVAL = 5000;

    private DeviceManager deviceManager;
    private ExceptionHelper exceptionHelper = ExceptionHelper.get();

    private BleManager bleManager;
    private MyHandler handler;
    private ConnectionHandler connectionHandler = ConnectionHandler.get();
    private MyStateListener stateListener;
    private ParseDataRunnable parseDataRunnable;

    //holds the sequence of flag bits to determine if there was a change
    private String lastFlagSequence = "00000000000";

    private GeneralBroadcastReceiver generalBroadcastReceiver = new GeneralBroadcastReceiver();

    //Broadcast receiver that listens for the ACTION_CONNECT broadcast from
    //the scanner window
    private ConnectionBroadcastReceiver connectionReceiver = new ConnectionBroadcastReceiver();

    //Intent filter for the connection receiver
    private IntentFilter connectionReceiverFilter = new IntentFilter();

    //The address to connect to after disconnecting to the current device
    private String pendingAddress = null;


    public BluetoothService(){}

    @Override
    public void onCreate()
    {
        super.onCreate();

        Console.d("service created");

        deviceManager = DeviceManager.get(getApplicationContext());
        bleManager = BleManager.get(getApplicationContext());
        handler = new MyHandler(this);

        connectionReceiverFilter.addAction(ACTION_CONNECT);
        connectionReceiverFilter.addAction(ACTION_DISCONNECT);
        connectionReceiverFilter.addAction(ACTION_CANCEL_CONNECTION);
        registerReceiver(connectionReceiver, connectionReceiverFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONFIG_CHANGE);
        filter.addAction(EXCEPTIONS_UPDATED);
        registerReceiver(generalBroadcastReceiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Console.d("service started");
        startForeground(NotificationHelper.SERVICE_NOTIFICATION, NotificationHelper.getServiceNotification());
        return START_STICKY;
    }

    private class GeneralBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action.equals(EXCEPTIONS_UPDATED))
            {
                lastFlagSequence = intent.getStringExtra("sequence");
            }

            if (action.equals(ACTION_CONFIG_CHANGE))
            {
                int selector = intent.getIntExtra("selector", -1);
                int value = intent.getIntExtra("value", -1);
            }
        }
    }

    /**
     * Listens for an ACTION_CONNECT broadcast
     */
    private class ConnectionBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action.equals(ACTION_CONNECT))
            {
                Console.v("Connect broadcast received");
                String address = intent.getStringExtra("address");

                if (connectionHandler.isConnected())
                {
                    Console.i("connection established, attempting to disconnect first");
                    pendingAddress = address;
                    handler.sendEmptyMessage(MSG_DISCONNECT);
                } else
                {
                    handler.sendMessage(getConnectMessage(address));
                }
            }

            if (action.equals(ACTION_DISCONNECT))
            {
                handler.sendEmptyMessage(MSG_DISCONNECT);
            }

            if (action.equals(ACTION_CANCEL_CONNECTION)){}
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Console.d("service destroyed");

        unregisterReceiver(connectionReceiver);
        unregisterReceiver(generalBroadcastReceiver);

        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent){return null;}

    private void sendBroadcast(String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    protected void connectToAddress(String address)
    {
        stateListener = new MyStateListener();
        bleManager.getDevice(address).connect(stateListener);
    }

    protected void disconnectFromCurrent()
    {
        connectionHandler.getDevice().disconnect();
    }

    protected void writePasscode()
    {
        Console.v("writing passcode");
        short value = Short.parseShort(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(Preferences.PASSCODE, "0000"));

        if (value == 0)
        {
            Console.i("passcode 0, quitting");
            sendBroadcast(PASSCODE_NULL);
            return;
        }

        byte[] data = ByteBuffer.allocate(2).putShort(value).array();

        connectionHandler.getDevice().write(Uuid.PASSCODE, data, new BleDevice.ReadWriteListener()
        {
            @Override
            public void onEvent(ReadWriteEvent e)
            {
                if (e.wasSuccess())
                {
                    Console.d("passcode written successfully");
                    new BackgroundThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Console.d("sleeping for 3 seconds");
                            try
                            {
                                Thread.sleep(3000);
                            } catch (InterruptedException e)
                            {
                            }

                            Console.d("checking passcode validity");
                            checkPasscodeValidity();
                        }
                    }).start();
                } else
                {
                    Console.w("passcode write failed");
                }
            }
        });
    }

    private void writeConfigChange(int selector, int value)
    {
        if (!connectionHandler.isConnected())
        {
            Console.i("failed config change, no connection");
            sendBroadcast(CONFIG_CHANGE_BAD);
            return;
        }

        BluetoothGattCharacteristic c = connectionHandler.getDevice().getNativeCharacteristic(Uuid.CONFIG_BASIC);

        if (c == null)
        {
            Console.i("failed config change, null characteristic");
            sendBroadcast(CONFIG_CHANGE_BAD);
            return;
        }

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

        connectionHandler.getDevice().write(Uuid.CONFIG_BASIC, data, new BleDevice.ReadWriteListener(){
            @Override
            public void onEvent(ReadWriteEvent e)
            {
                if (e.status() == BleDevice.ReadWriteListener.Status.SUCCESS)
                {
                    Console.i("wrote Configuration Change.");
                    sendBroadcast(CONFIG_CHANGE_GOOD);
                } else
                {
                    Console.i("failed Configuration Change.");
                    sendBroadcast(CONFIG_CHANGE_BAD);
                }
            }
        });
    }

    private void checkPasscodeValidity()
    {
        connectionHandler.getDevice().read(Uuid.STATUS_BASIC, new BleDevice.ReadWriteListener()
        {
            @Override
            public void onEvent(ReadWriteEvent e)
            {
                if (e.data()[19] == 1) //bad passcode response
                {
                    //bad response
                    Console.w("passcode declined");
                    connectionHandler.intentionalDisconnect();
                    sendBroadcast(PASSCODE_DECLINED);

                } else if (e.data()[19] == 0) //good passcode response
                {
                    //good response
                    Console.i("passcode accepted");
                    deviceManager.newDevice(connectionHandler.getAddress());
                    sendBroadcast(PASSCODE_ACCEPTED);

                    connectionHandler.getDevice().enableNotify(Uuid.STATUS_BASIC, new NotifyListener());
                }
            }
        });
    }

    private Message getConnectMessage(String address)
    {
        Message m = new Message();
        m.what = MSG_CONNECT;
        Bundle args = new Bundle();
        args.putString("address", address);
        m.setData(args);
        return m;
    }

    private class MyStateListener implements BleDevice.StateListener
    {
        @Override
        public void onEvent(StateEvent e)
        {
            /**
             * Handles connection and bonding
             */
            if (e.didEnter(BleDeviceState.INITIALIZED))
            {
                Console.i("connection intialized");
                connectionHandler.setState(ConnectionHandler.STATE_CONNECTED, e.device());

                //reset pending address
                pendingAddress = null;

                //check if we are already bonded
                if (e.device().is(BleDeviceState.BONDED))
                {
                    Console.i("bonded");
                    connectionHandler.setState(ConnectionHandler.STATE_BONDED);
                    handler.sendEmptyMessage(MSG_WRITE_PASSCODE);
                }
                else
                {
                    Console.i("bonding");
                    connectionHandler.setState(ConnectionHandler.STATE_BONDING);
                    e.device().bond(new BleDevice.BondListener()
                    {
                        @Override
                        public void onEvent(BondEvent e)
                        {
                            if (e.wasSuccess())
                            {
                                Console.i("bonded");
                                connectionHandler.setState(ConnectionHandler.STATE_BONDED);
                                handler.sendEmptyMessage(MSG_WRITE_PASSCODE);
                            } else
                            {
                                Console.w("failed to bond [" + e.failReason() + "]");
                                handler.sendEmptyMessage(MSG_DISCONNECT);
                            }
                        }
                    });
                }
            }

            if (e.didEnter(BleDeviceState.DISCONNECTED))
            {
                State.ChangeIntent state = e.device().getLastDisconnectIntent();
                if (state == State.ChangeIntent.INTENTIONAL)
                {
                    Console.i("disconnected");
                    connectionHandler.setState(ConnectionHandler.STATE_DISCONNECTED);
                    connectionHandler.intentionalDisconnect();
                    sendBroadcast(DISCONNECTED_SUCCESSFULLY);

                    //Connect to next if switching between two devices
                    if (pendingAddress != null)
                    {
                        handler.sendMessage(getConnectMessage(pendingAddress));
                    }
                }
                else
                {
                    Console.i("lost connection");
                    connectionHandler.setState(ConnectionHandler.STATE_UNINTENTIONAL_DISCONNECT);
                }
            }
        }
    }

    private class NotifyListener implements BleDevice.ReadWriteListener
    {
        @Override
        public void onEvent(ReadWriteEvent e)
        {
            if (e.data().length == 20)
            {
                connectionHandler.setLastDataUpdateTime(System.currentTimeMillis());
                parseDataRunnable = new ParseDataRunnable(getApplicationContext(), e.device(), e.data(), lastFlagSequence);
                new BackgroundThread(parseDataRunnable).start();
            }
        }
    }

    private static class MyHandler extends Handler
    {
        private final WeakReference<BluetoothService> service;

        public MyHandler(BluetoothService service)
        {
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg)
        {
            BluetoothService s = service.get();
            switch (msg.what)
            {
                case MSG_CONNECT:
                    s.connectToAddress(msg.getData().getString("address"));
                    break;
                case MSG_DISCONNECT:
                    s.disconnectFromCurrent();
                    break;
                case MSG_WRITE_PASSCODE:
                    s.writePasscode();
                    break;
            }
        }
    }

}
