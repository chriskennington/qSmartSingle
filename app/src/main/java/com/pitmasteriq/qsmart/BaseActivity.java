package com.pitmasteriq.qsmart;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.utils.Interval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;




public abstract class BaseActivity extends Activity implements ScanFragment.ScanClickListener,
        ParameterEditorDialog.ParameterEditorListener,
        InfoFragment.InfoFragmentListener
{
    private static final UUID[] WHITELIST = new UUID[]{Uuid.SERVICE, Uuid.CONFIG_BASIC, Uuid.STATUS_BASIC, Uuid.PASSCODE};

    public static final String NOTIFICATION_CANCELED = "notification_canceled";
    public static final String NOTIFICATION_ACK = "notification_ack";
    public static final int NOTIFICATION_WAIT_TIME = 60000;
    private static final int CONNECTION_TIMEOUT_LENGTH = 30000;
    protected static final int UPDATE_INTERVAL = 2000;
    protected static final int MIN_SWIPE_DISTANCE = 50;

    private Context context;
    protected boolean scannerAnimating = false;
    protected static boolean isVisible = false;
    protected Handler handler = new Handler();
    public BluetoothService service;
    private boolean serviceBound = false;
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;
    protected static BleManager bleManager;
    protected static DeviceManager deviceManager;
    private ProgressDialog pd;
    protected Typeface seg7Font;
    private boolean waitingForDisconnect = false;

    //used for detecting swipe
    private float x1,x2;


    //*** ACTION BAR BUTTONS ******
    protected ImageView scannerButton, settingsButton, infoButton;
    protected TextView actionBarTitle;




    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.context = this;

        showCustomActionBar();


        prefs = getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        editor = prefs.edit();

        BleManagerConfig config = new BleManagerConfig()
        {{
                this.defaultScanFilter = new BleManagerConfig.DefaultScanFilter(Uuid.SERVICE);
                this.autoScanTime = Interval.millis(1000);
                this.autoScanInterval = Interval.millis(2000);
                this.autoScanIntervalWhileAppIsPaused = Interval.millis(5000);
                this.defaultScanFilter = new BleManagerConfig.DefaultScanFilter(new HashSet<UUID>(Arrays.asList(WHITELIST)));
        }};

        bleManager = BleManager.get(getApplicationContext(), config);

        deviceManager = DeviceManager.get(getApplicationContext());

        seg7Font = Typeface.createFromAsset(getAssets(), "fonts/segmental.ttf");


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        handler.post(getUiRunnable());

        stopScannerAnimation();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(serviceBound)
        {
            unbindService(serviceConnection);
            serviceBound = false;
        }

        deviceManager.saveDevice();

        handler.removeCallbacksAndMessages(null);

        if(prefs.getBoolean(Preferences.SHUTTING_DOWN, false))
        {
            Log.w("base activity", "shutting down");
            editor.putBoolean(Preferences.SHUTTING_DOWN, false).commit();

            bleManager = null;
            deviceManager.clear();
            deviceManager = null;
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        bleManager.onResume();


        //stop alarm
        if(prefs.getBoolean(Preferences.ALARM_SOUNDING, false))
        {
            editor.putBoolean(Preferences.ALARM_SOUNDING, false).commit();
            ExceptionManager.get(getApplicationContext()).stopAlarm();
        }

        isVisible = true;
        editor.putBoolean(Preferences.HAS_FOREGROUND, true).commit();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        bleManager.onPause();

        isVisible = false;
        editor.putBoolean(Preferences.HAS_FOREGROUND, false).commit();
    }

    @Override
    public void onApplicationClose()
    {

        service.disconnectFromDevice();
        stopService(new Intent(this, BluetoothService.class));

        editor.putBoolean(Preferences.SHUTTING_DOWN, true).commit();

        finish();
    }

    @Override
    public void onScanClick(String address)
    {

        pd = new ProgressDialog(this);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.setMessage("Attempting to connect to your device. Please wait up to 30 seconds");
        pd.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                service.cancelConnectionAttempt(false);
            }
        });
        pd.show();

        deviceManager.newDevice(address);

        String connectedAddress = prefs.getString(Preferences.CONNECTED_ADDRESS, null);

        if (connectedAddress == null || connectedAddress.equals(address))
        {
            editor.putString(Preferences.CONNECTED_ADDRESS, null).commit();
            service.connectToDevice(address);
        }
        else
        {
            waitingForDisconnect = true;
            service.disconnectFromDevice();
        }

        handler.postDelayed(connectionTimer, CONNECTION_TIMEOUT_LENGTH);
    }

    protected abstract Runnable getUiRunnable();


    private void showCustomActionBar()
    {
        ActionBar ab = this.getActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.action_bar, null);

        ab.setCustomView(v);
        ab.setDisplayShowCustomEnabled(true);

        scannerButton = (ImageView) v.findViewById(R.id.action_bar_scanner);
        settingsButton = (ImageView) v.findViewById(R.id.action_bar_settings);
        infoButton = (ImageView) v.findViewById(R.id.action_bar_info);
        actionBarTitle = (TextView) v.findViewById(R.id.action_bar_title);
    }

    protected void openScanFragment()
    {
        ScannedDevices.get().clearHasNew();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        removePreviousFragment(ft);

        ScanFragment scan = new ScanFragment();

        String currentAddress = null;

        if(deviceManager.device() != null)
            currentAddress = deviceManager.device().getAddress();

        Bundle args = new Bundle();
        args.putString("address", currentAddress);
        scan.setArguments(args);


        try
        {
            scan.show(ft, "dialog");
        }
        catch(IllegalStateException e){e.printStackTrace();}

    }


    private void openInfoFragment()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        removePreviousFragment(ft);



        InfoFragment info = new InfoFragment();
        Bundle args = new Bundle();
        try
        {
            String v1 = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String v2 = Build.VERSION.RELEASE;
            String m1 = Build.MANUFACTURER;
            String m2 = Build.MODEL;

            args.putString("app_version", v1);
            args.putString("android_version", v2);
            args.putString("manuf", m1);
            args.putString("model", m2);
        }
        catch(PackageManager.NameNotFoundException e)
        {
            args.putString("version", "0.0.0");
        };

        info.setArguments(args);
        info.show(ft, "dialog");
    }

    protected void openExceptionFragment()
    {
        if(deviceManager.device() != null)
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            removePreviousFragment(ft);

            ExceptionFragment frag = new ExceptionFragment();
            frag.show(ft, "dialog");
        }
    }



    private void removePreviousFragment(FragmentTransaction fragmentTransaction)
    {
        FragmentTransaction ft = fragmentTransaction;

        Fragment prevFrag = getFragmentManager().findFragmentByTag("dialog");
        if(prevFrag != null)
            ft.remove(prevFrag).commit();
    }




    protected void openParameterDialog(String title, int current, int selector)
    {


        DialogFragment edit = new ParameterEditorDialog();

        if (selector == DeviceConfig.CONFIG_DELAY_TIME)
            edit = new DelayTimeEditorDialog();

        if (selector == TextEditorDialog.FOOD1_NAME ||
                selector == TextEditorDialog.FOOD2_NAME ||
                selector == TextEditorDialog.DEVICE_NAME)
        {
            edit = new TextEditorDialog();
        }

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("current", current);
        args.putInt("selector", selector);

        edit.setArguments(args);
        edit.show(getFragmentManager(), "dialog");
    }

    @Override
    public void parameterChanged(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            int selector = data.getIntExtra("selector", -1);
            service.updateConfigurationValue(selector, data.getIntExtra("value", -1));
        }
        else
        {
            int min = data.getIntExtra("min", -1);
            int max = data.getIntExtra("max", -1);

            String message = "";
            if ( min != -1 && max != -1 )
                message = "Min value: " + min + "\nMax value: " + max;

            MessageDialog md = MessageDialog.newInstance("Value out of range", message);
            md.show(getFragmentManager(), "dialog");
        }
    }

    private BluetoothService.ServiceListener serviceListener = new BluetoothService.ServiceListener()
    {

        @Override
        public void connectionAttemptCanceled()
        {
            clearConnectionProgressDialog();

            handler.removeCallbacks(connectionTimer);

            try
            {
                //show connection canceled  message
                MessageDialog md = MessageDialog.newInstance("Notice", "Connection attempt canceled");
                md.show(getFragmentManager(), "dialog");
            }
            catch(IllegalStateException e){e.printStackTrace();}
        }

        @Override
        public void connectionFailed(String msg, String address)
        {
            //hide adding iq progress dialog
            clearConnectionProgressDialog();

            handler.removeCallbacks(connectionTimer);

            try
            {
                //show device added message
                MessageDialog md = MessageDialog.newInstance("Error", "Connection Failed: " + msg);
                //md.show(getFragmentManager(), "dialog");

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                ScannedDevices.get().addressUndiscovered(address);
            }
            catch(IllegalStateException e){e.printStackTrace();}

        }


        @Override
        public void newDeviceAdded(String address, boolean reconnected)
        {
            //hide adding iq progress dialog
            clearConnectionProgressDialog();

            handler.removeCallbacks(connectionTimer);

            if (!reconnected) //if the device connected on a reconnect attempt, dont show the device connected dialog
            {
                try
                {
                    //show device added message
                    MessageDialog md = MessageDialog.newInstance("Success", "Device connected successfully!");
                    md.show(getFragmentManager(), "dialog");
                } catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void newDeviceFailed(String address)
        {
            clearConnectionProgressDialog();

            handler.removeCallbacks(connectionTimer);

            try
            {
                //show device not added message
                MessageDialog md = MessageDialog.newInstance("Failed", "Device could not be connected to. This could be due to an incorrect passcode. The passcode can not be '0000'.");
                md.show(getFragmentManager(), "dialog");
            }
            catch(IllegalStateException e){e.printStackTrace();}
        }

        @Override
        public void newDeviceTimeout(String address)
        {
            clearConnectionProgressDialog();

            //show device not added message
            try
            {
                MessageDialog md = MessageDialog.newInstance("Failed", "Connection attempt timed out, please try again.");
                md.show(getFragmentManager(), "dialog");
            }
            catch(IllegalStateException e){e.printStackTrace();}
        }

        @Override
        public void configurationChangeFailed()
        {

        }

        @Override
        public void configurationChangeSucceeded()
        {
            try
            {
                AssetFileDescriptor afd = context.getAssets().openFd("audio" + File.separator + "config_edit_success.mp3");
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                player.prepare();
                player.start();
            }
            catch(IOException e){e.printStackTrace();}
        }

        @Override
        public void disconnectFailed()
        {
            if(waitingForDisconnect)
            {
                waitingForDisconnect = false;
                service.connectToDevice(deviceManager.device().getAddress());
            }
        }

        @Override
        public void disconnectSucceeded()
        {
            if(waitingForDisconnect)
            {
                waitingForDisconnect = false;
                service.connectToDevice(deviceManager.device().getAddress());
            }
        }
    };



    protected void startScannerAnimation()
    {
        final AnimationDrawable ad = (AnimationDrawable) scannerButton.getDrawable();
        ad.start();

        scannerAnimating = true;
    }

    protected void stopScannerAnimation()
    {
        final AnimationDrawable ad = (AnimationDrawable) scannerButton.getDrawable();
        ad.stop();
        ad.selectDrawable(0);

        scannerAnimating = false;
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder s)
        {
            service = ((BluetoothService.BluetoothBinder)s).getService();
            service.setServiceListener(serviceListener);
            serviceBound = true;

            bleManager.setListener_Discovery(service.discoveryListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            serviceBound = false;
        }
    };


    private Runnable connectionTimer = new Runnable()
    {
        @Override
        public void run()
        {
            service.connectionAttemptTimedOut(deviceManager.device().getAddress());
        }
    };


    public void actionClick(View v)
    {
        switch( v.getId() )
        {
            case R.id.action_bar_info:
                openInfoFragment();
                break;
            case R.id.action_bar_scanner:
                openScanFragment();
                stopScannerAnimation();
                break;
            case R.id.action_bar_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }


    protected void updateStatusIcon(ImageView v, int blinkRate)
    {
        Device d = deviceManager.device();

        if (d != null)
        {
            switch (d.getStatus())
            {
                case Device.Status.OK:
                    v.setImageResource(R.drawable.status_icon_green);
                    break;
                case Device.Status.NoData:
                    v.setImageResource(R.drawable.status_icon_yellow);
                    break;
                case Device.Status.Disconnected:
                    v.setImageResource(R.drawable.status_icon_gray);
                    break;
                case Device.Status.LostConnection:
                case Device.Status.Unknown:
                    v.setImageResource(R.drawable.status_icon_red);
                    break;
            }

            if(d.exceptions().hasException())
            {
                if( blinkRate > 0 )
                    v.setAnimation(Animations.getBlinkAnimation(blinkRate));
            }
            else if (d.exceptions().hasSilenced())
            {
                if (blinkRate == 0)
                    blinkRate = 750;

                v.setAnimation(Animations.getPulseAnimation(blinkRate));
            }
            else
            {
                v.clearAnimation();
            }
        }
        else
            //device is null
            v.setImageResource(R.drawable.status_icon_gray);
    }

    private void clearConnectionProgressDialog()
    {
        if(pd!=null)
        {
            pd.setOnCancelListener(null);
            pd.cancel();
        }
    }
}
