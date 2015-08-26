package com.pitmasteriq.qsmart;

import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Interval;

import java.nio.ByteBuffer;

/**
 * Created by Chris on 4/28/2015.
 */
public class ConnectionTransaction
{

    private static final int TIMEOUT_LENGTH = 30000;

    private ConnectionListener listener;
    private Context context;
    private DeviceManager deviceManager;
    private BleDevice device;
    private byte[] data;

    private boolean passcodeAccepted = false;

    private Handler handler = new Handler();

    private static final String BAD_RESPONSE = "00000000000000000001";
    private static final String WAITING_FOR_RESPONSE = "00000000000000000000";


    public ConnectionTransaction(Context context)
    {
        this.context = context;
        deviceManager = DeviceManager.get(context);
    }

    public void setConnectionListener(ConnectionListener listener){this.listener = listener;}

    public void start(final BleDevice device)
    {

        //Send passcode after delay to prevent device BLE module from freaking out!
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                writePasscode(device);
            }
        }, 1000);

    }

    public void cancel()
    {
        try
        {
            handler.removeCallbacksAndMessages(timer);
            device.disconnect();
        }
        catch(Exception e){e.printStackTrace();}
    }

    private void writePasscode(final BleDevice device)
    {
        this.device = device;

        short value = Short.parseShort(PreferenceManager.getDefaultSharedPreferences(context).getString(Preferences.PASSCODE, "0000"));

        if (value == 0)
        {
            device.disconnect();
            listener.onPasscodeFailed(device);
            return;
        }

        //TODO get actual password

        data = ByteBuffer.allocate(2).putShort( value ).array();
        Log.w("TAG", getResponseString(data)[1]);

        device.write(Uuid.PASSCODE, data, new BleDevice.ReadWriteListener()
        {
            @Override
            public void onEvent(ReadWriteEvent e)
            {
                if (e.wasSuccess())
                {
                    handler.postDelayed(timer, TIMEOUT_LENGTH);

                    if(device.isNotifyEnabled(Uuid.STATUS_BASIC))
                        e.device().disableNotify(Uuid.STATUS_BASIC);

                    device.enableNotify(Uuid.STATUS_BASIC, Interval.TEN_SECS, notifyListener);
                }
            }
        });
    }

    private BleDevice.ReadWriteListener notifyListener = new BleDevice.ReadWriteListener()
    {
        @Override
        public void onEvent(ReadWriteEvent e)
        {
            if( e.data().length == 20 )
            {
                String[] responseStrings = getResponseString(e.data());
                Log.d("TAG", responseStrings[1]); //print formatted version

                if( responseStrings[0].equals(WAITING_FOR_RESPONSE) )
                {
                    Log.i("trans", "Waiting for response from module: " + e.device().getMacAddress());
                }
                else if( responseStrings[0].equals(BAD_RESPONSE) )
                {
                    Log.i("trans", "Passcode incorrect: " + e.device().getMacAddress());
                    e.device().disableNotify(Uuid.STATUS_BASIC, this);
                    e.device().disconnect();

                    handler.removeCallbacks(timer);
                    listener.onPasscodeFailed(e.device());
                }
                else
                {
                    handler.removeCallbacks(timer);
                    if( !passcodeAccepted ) //NEW DEVICE!
                    {
                        passcodeAccepted = true;

                        Log.i("trans", "Passcode correct: " + e.device().getMacAddress());
                        listener.onPasscodeAccepted(e.device());
                        listener.onDataReceived(e.device(), e.data());
                    }
                    else
                        listener.onDataReceived(e.device(), e.data());
                }
            }
        }
    };

    private static String[] getResponseString(byte[] data)
    {
        String dataString = "";
        String dataStringFormatted = "";
        for( byte b : data )
        {
            dataString += b;
            dataStringFormatted += b + "|";
        }

        return new String[]{dataString, dataStringFormatted};
    }

    public interface ConnectionListener
    {
        void onDataReceived(BleDevice device, byte[] data);
        void onPasscodeAccepted(BleDevice device);
        void onPasscodeFailed(BleDevice device);
        void onPasscodeTimeout(BleDevice device);
    }

    private Runnable timer = new Runnable()
    {
        @Override
        public void run()
        {
            device.disableNotify(Uuid.STATUS_BASIC);
            device.disconnect();

            listener.onPasscodeTimeout(device);
        }
    };
}
