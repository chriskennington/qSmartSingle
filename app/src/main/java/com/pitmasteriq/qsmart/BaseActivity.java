package com.pitmasteriq.qsmart;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.utils.Interval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class BaseActivity extends Activity
{
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    //*** ACTION BAR BUTTONS ******
    protected ImageView scannerButton, settingsButton, infoButton;
    protected TextView actionBarTitle;

    private BluetoothService service;
    private boolean serviceBound = false;
    private BleManager bleManager;
    private static final UUID[] WHITELIST = new UUID[]{Uuid.SERVICE, Uuid.CONFIG_BASIC, Uuid.STATUS_BASIC, Uuid.PASSCODE};



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

        BleManagerConfig config = new BleManagerConfig()
        {{
                this.defaultScanFilter = new BleManagerConfig.DefaultScanFilter(Uuid.SERVICE);
                this.autoScanTime = Interval.millis(1000);
                this.autoScanInterval = Interval.millis(2000);
                this.autoScanIntervalWhileAppIsPaused = Interval.millis(5000);
                this.defaultScanFilter = new BleManagerConfig.DefaultScanFilter(new HashSet<UUID>(Arrays.asList(WHITELIST)));
            }};

        bleManager = BleManager.get(getApplicationContext(), config);
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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        bleManager.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        bleManager.onResume();
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
        AnimationDrawable ad = (AnimationDrawable) scannerButton.getDrawable();
        ad.stop();
        ad.selectDrawable(2);

        settingsButton = (ImageView) v.findViewById(R.id.action_bar_settings);
        infoButton = (ImageView) v.findViewById(R.id.action_bar_info);
        actionBarTitle = (TextView) v.findViewById(R.id.action_bar_title);
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
        public void passcodeDeclined()
        {
            
        }
    };
}
