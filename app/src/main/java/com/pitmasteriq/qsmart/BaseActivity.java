package com.pitmasteriq.qsmart;


import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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

public class BaseActivity extends Activity implements FragmentResponseListener, ParameterEditedListener
{
    public static final String NOTIFICATION_CANCELED = "notification_canceled";
    public static final String NOTIFICATION_ACK = "notification_ack";
    public static final int NOTIFICATION_WAIT_TIME = 60000;
    private static final int CONNECTION_TIMEOUT_LENGTH = 30000;
    protected static final int UPDATE_INTERVAL = 2000;
    protected static final int MIN_SWIPE_DISTANCE = 50;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    //*** ACTION BAR BUTTONS ******
    protected ImageView scannerButton, settingsButton, infoButton;
    protected TextView actionBarTitle;

    private BluetoothService service;
    private boolean serviceBound = false;
    private BleManager bleManager;
    private static final UUID[] WHITELIST = new UUID[]{Uuid.SERVICE, Uuid.CONFIG_BASIC, Uuid.STATUS_BASIC, Uuid.PASSCODE};

    private SharedPreferences userPrefs;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private ProgressDialog pd;
    private boolean shuttingDown = false;


    private DeviceManager deviceManager;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        showCustomActionBar();
        setContentView(R.layout.activity_base);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        userPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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

        checkForFirstLaunch();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                default:
                case 0: return StandardFragment.newInstance();


                case 1: return AdvancedFragment.newInstance();

            }
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "Standard";
                case 1:
                    return "Advanced";
            }
            return null;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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

        if (shuttingDown)
        {
            bleManager = null;
            deviceManager.clear();
            deviceManager = null;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        MyApplication.activityPaused();
        bleManager.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MyApplication.activityResumed();
        bleManager.onResume();

        if(prefs.getBoolean(Preferences.NOTIFY_SOUNDING, false))
        {
            ExceptionManager.get(getApplicationContext()).cancelNotification(ExceptionManager.ALARM);
        }
    }

    @Override
    public void onFragmentResponse(FragmentResponseEvent e)
    {
        switch (e.type())
        {
            case FragmentResponseEvent.CONNECT_TO_ADDRESS:
                showLoadingDialog();
                service.connectToAddress(e.stringData());
                break;
            case FragmentResponseEvent.APPLICATION_CLOSE:
                shutdownApp();
                break;
        }
    }

    @Override
    public void onParameterChanged(ParameterEditedEvent e)
    {
        if (e.resultCode == Activity.RESULT_OK)
        {
            int selector = e.data().getIntExtra("selector", -1);
            int value = e.data().getIntExtra("value", -1);

            service.updateConfigurationValue(selector, value);
        }
        else
        {
            int min = e.data().getIntExtra("min", -1);
            int max = e.data().getIntExtra("max", -1);

            String message = "";
            if ( min != -1 && max != -1 )
                message = "Min value: " + min + "\nMax value: " + max;

            MessageDialog md = MessageDialog.newInstance("Value out of range", message);
            md.show(getFragmentManager(), "dialog");
        }
    }

    private void shutdownApp()
    {
        shuttingDown = true;

        service.disconnectFromDevice();
        stopService(new Intent(this, BluetoothService.class));
        finish();
    }

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

    /**
     * Checks if the application is new to the phone (never opened)
     * @return
     */
    private boolean checkForFirstLaunch()
    {
        if( prefs.getBoolean(Preferences.FIRST_LAUNCH, true) )
        {
            //set important default preferences
            editor.putBoolean(Preferences.FIRST_LAUNCH, false).apply();
            editor.commit();

            //MessageDialog md1 = MessageDialog.newInstance("test", "test", "ok");
            //md1.show(getFragmentManager(), "dialog");

            MessageDialog md = MessageDialog.newInstance("Disclaimer", "Read all safety warnings in the IQ130 Setup and Operating Instructions Manual.  Never leave a charcoal fire unattended!", "Agree");
            md.show(getFragmentManager(), "dialog");

            Toast.makeText(this, "first launch", Toast.LENGTH_LONG).show();

            return true;
        }

        return false;
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

    private BluetoothService.ServiceListener serviceListener = new BluetoothService.ServiceListener()
    {
        @Override
        public void connectFailed()
        {
            hideLoadingDialog();
        }

        @Override
        public void connectSucceeded()
        {
            hideLoadingDialog();
        }

        @Override
        public void disconnectFailed(){}

        @Override
        public void disconnectSucceeded(){}

        @Override
        public void passcodeAccepted()
        {
            hideLoadingDialog();
            Toast.makeText(getApplicationContext(),"Device Connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void passcodeDeclined()
        {
            hideLoadingDialog();

            MessageDialog md = MessageDialog.newInstance("Failed", "Device could not be connected to. This could be due to an incorrect passcode. The passcode can not be '0000'.");
            md.show(getFragmentManager(), "dialog");
        }

        @Override
        public void configChangeFailed(){}

        @Override
        public void configChangeSucceeded()
        {
            try
            {
                AssetFileDescriptor afd = getApplicationContext().getAssets().openFd("audio" + File.separator + "config_edit_success.mp3");
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                player.prepare();
                player.start();
            }
            catch(IOException e){e.printStackTrace();}
        }
    };

    public void openInfoFragment(View v)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        removePreviousFragment(ft);


        InfoFragment info = new InfoFragment();

        Bundle args = new Bundle();
        args.putString("app_version", BuildConfig.VERSION_NAME);
        args.putString("android_version", Build.VERSION.RELEASE);
        args.putString("manuf", Build.MANUFACTURER);
        args.putString("model", Build.MODEL);

        info.setArguments(args);

        try
        {
            info.show(ft, "dialog");
        }
        catch(IllegalStateException e){e.printStackTrace();}
    }

    public void openScanFragment(View v)
    {
        ScannedDevices.get().clearHasNew();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        removePreviousFragment(ft);

        ScanFragment scan = new ScanFragment();

        String currentAddress = null;

        try
        {
            currentAddress = deviceManager.device().getAddress();
        }
        catch(NullDeviceException e){}

        Bundle args = new Bundle();
        args.putString("address", currentAddress);
        scan.setArguments(args);


        try
        {
            scan.show(ft, "dialog");
        }
        catch(IllegalStateException e){e.printStackTrace();}
    }

    public void openSettings(View v)
    {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void removePreviousFragment(FragmentTransaction fragmentTransaction)
    {
        FragmentTransaction ft = fragmentTransaction;

        Fragment prevFrag = getFragmentManager().findFragmentByTag("dialog");
        if(prevFrag != null)
            ft.remove(prevFrag).commit();
    }

    public void openExceptions(View v)
    {
        if(deviceManager.hasDevice())
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            removePreviousFragment(ft);

            ExceptionFragment frag = new ExceptionFragment();
            frag.show(ft, "dialog");
        }
    }

    public void changeDeviceName(View v)
    {
        Toast.makeText(this, "changing name", Toast.LENGTH_SHORT).show();
        openParameterDialog("Change Device Name", 0, TextEditorDialog.DEVICE_NAME);
    }

    public void changeProbe1Name(View v)
    {
        openParameterDialog("Change Food Probe 1 Name", 0, TextEditorDialog.FOOD1_NAME);
    }

    public void changeProbe2Name(View v)
    {
        openParameterDialog("Change Food Probe 2 Name", 0, TextEditorDialog.FOOD2_NAME);
    }

    public void changePitSet(View v)
    {
        try
        {
            openParameterDialog("Change Pit Set Temperature", deviceManager.device().config().pitSet().get(), DeviceConfig.CONFIG_PIT_SET);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changePitDeviation(View v)
    {
        try
        {
            openParameterDialog("Change Pit Temp Deviation", deviceManager.device().config().pitAlarmDeviation().getRelative(), DeviceConfig.CONFIG_PIT_ALARM);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeFood1Alarm(View v)
    {
        try
        {
            openParameterDialog("Change Food 1 Alarm Temp", deviceManager.device().config().food1AlarmTemp().get(), DeviceConfig.CONFIG_FOOD_1_ALARM);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeFood2Alarm(View v)
    {
        try
        {
            openParameterDialog("Change Food 2 Alarm Temp", deviceManager.device().config().food2AlarmTemp().get(), DeviceConfig.CONFIG_FOOD_2_ALARM);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeDelayPitSet(View v)
    {
        try
        {
            openParameterDialog("Change Delay Pit Set", deviceManager.device().config().delayPitSet().get(), DeviceConfig.CONFIG_DELAY_PIT_SET);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeDelayTime(View v)
    {
        try
        {
            openParameterDialog("Change Delay Time", deviceManager.device().config().getDelayTime(), DeviceConfig.CONFIG_DELAY_TIME);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeFood1PitSet(View v)
    {
        try
        {
            openParameterDialog("Change Food 1 Pit Set", deviceManager.device().config().food1PitSet().get(), DeviceConfig.CONFIG_FOOD_1_PIT_SET);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeAtFood1Temp(View v)
    {
        try
        {
            openParameterDialog("Change Pit Set at Food 1 Temp", deviceManager.device().config().food2PitSet().get(), DeviceConfig.CONFIG_FOOD_1_TEMP);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeFood2PitSet(View v)
    {
        try
        {
            openParameterDialog("Change Food 2 Pit Set", deviceManager.device().config().food1Temp().get(), DeviceConfig.CONFIG_FOOD_2_PIT_SET);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    public void changeAtFood2Temp(View v)
    {
        try
        {
            openParameterDialog("Change Pit Set at Food 2 Temp", deviceManager.device().config().food2Temp().get(), DeviceConfig.CONFIG_FOOD_2_TEMP);
        } catch (NullDeviceException e) {e.printStackTrace();}
    }

    private void openParameterDialog(String title, int current, int selector)
    {
        if (!deviceManager.hasDevice())
            return;

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        removePreviousFragment(ft);


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
        edit.show(ft, "dialog");
    }

    private void showLoadingDialog()
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
                service.cancelConnectionAttempt();
            }
        });
        pd.show();
    }

    private void hideLoadingDialog()
    {
        if(pd!=null)
        {
            pd.setOnCancelListener(null);
            pd.cancel();
        }
    }
}
