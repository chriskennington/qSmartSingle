package com.pitmasteriq.qsmartsingle;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idevicesinc.sweetblue.BleDevice;


public class StandardMonitorActivity extends BaseActivity
{

    //used for detecting swipe
    private float x1,x2;

    //*** UI COMPONENTS ***
    private TextView deviceName, food1Name, food2Name, pitSet, pitTemp, food1Temp, food2Temp, debug;
    private ImageView statusIcon;
    //*********************

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_monitor);

        isFirstLaunch();
        isNewVersion();


        deviceName = (TextView)findViewById(R.id.standard_device_name);
        food1Name = (TextView)findViewById(R.id.standard_food_1_name);
        food2Name = (TextView)findViewById(R.id.standard_food_2_name);
        pitSet = (TextView)findViewById(R.id.standard_pit_set_value);
        pitTemp = (TextView)findViewById(R.id.standard_pit_temp_value);
        food1Temp = (TextView)findViewById(R.id.standard_food_1_temp);
        food2Temp = (TextView)findViewById(R.id.standard_food_2_temp);
        debug = (TextView)findViewById(R.id.standard_debug);
        statusIcon = (ImageView)findViewById(R.id.standard_status_icon);


        statusIcon.setOnClickListener(statusIconClick);

        pitSet.setTypeface(seg7Font);
        pitTemp.setTypeface(seg7Font);
        food1Temp.setTypeface(seg7Font);
        food2Temp.setTypeface(seg7Font);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(prefs.getBoolean(Preferences.NOTIFY_SOUNDING, false))
        {
            ExceptionManager.get(getApplicationContext()).cancelNotification(ExceptionManager.ALARM);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE)
                {
                    if(x2 < x1)
                    {
                        Toast.makeText(this, "swiped left", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdvancedMonitorActivity.class));
                        finish();
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_standard_monitor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        scannerIcon = menu.findItem(R.id.action_scanner);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_scanner:
                stopScannerAnimation();
                openScanFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

    @Override
    protected Runnable getUiRunnable()
    {
        return new UpdateUiRunnable();
    }

    public void parameterChange(View v)
    {
        if(deviceManager.device() != null)
        {
            switch (v.getId())
            {
                case R.id.standard_device_name:
                    break;
                case R.id.standard_pit_set_click_loc:
                    openParameterDialog("Change Pit Set Temperature", deviceManager.device().config().getPitSet(), DeviceConfig.CONFIG_PIT_SET);
                    break;
                case R.id.standard_food_1_click_loc:
                    break;
                case R.id.standard_food_2_click_loc:
                    break;
            }
        }
    }

    /**
     * Checks if the application is new to the phone (never opened)
     * @return
     */
    private boolean isFirstLaunch()
    {
        if( prefs.getBoolean(Preferences.FIRST_LAUNCH, true) )
        {
            //set important default preferences
            editor.putBoolean(Preferences.FIRST_LAUNCH, false).apply();
            editor.commit();

            MessageDialog md = MessageDialog.newInstance("Disclaimer", "Read all safety warnings in the IQ130 Setup and Operating Instructions Manual.  Never leave a charcoal fire unattended!", "Agree");
            md.show(getFragmentManager(), "dialog");

            Toast.makeText(this, "first launch", Toast.LENGTH_LONG).show();

            return true;
        }

        return false;
    }

    /**
     * Checks if the application was updated since last used
     * @return
     */
    private boolean isNewVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;

            int currentVersion = prefs.getInt(Preferences.VERSION_CODE, -1);
            if( currentVersion == -1 || currentVersion != versionCode )
            {
                editor.putBoolean(Preferences.NEW_VERSION, true);
                editor.putInt(Preferences.VERSION_CODE, versionCode);
                editor.apply();
            }
        } catch(PackageManager.NameNotFoundException e){e.printStackTrace();}

        if( prefs.getBoolean(Preferences.NEW_VERSION, false) )
        {
            editor.putBoolean(Preferences.NEW_VERSION, false);
            editor.apply();

            Toast.makeText(this, "new version", Toast.LENGTH_LONG).show();

            //MessageDialog md = MessageDialog.newInstance("Update", getString(R.string.version_0_1_1_notes));
            //md.show(getFragmentManager(), "dialog");

            return true;
        }

        return false;
    }

    private View.OnClickListener statusIconClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            openExceptionFragment();
        }
    };

    private class UpdateUiRunnable implements Runnable
    {
        @Override
        public void run()
        {
            int blinkRate = 0;

            if(ScannedDevices.get().hasNew())
                if (!scannerAnimating)
                    startScannerAnimation();


            Device d = deviceManager.device();
            if(d != null)
            {
                deviceName.clearAnimation();
                pitTemp.clearAnimation();
                food1Temp.clearAnimation();
                food2Temp.clearAnimation();


                deviceName.setText(d.getDefinedName());
                food1Name.setText(d.food1Probe().getName());
                food2Name.setText(d.food2Probe().getName());

                pitSet.setText(String.valueOf(d.config().getPitSet()));



                if(d.pitProbe().getTemperature() != 999)
                    pitTemp.setText(String.valueOf(d.pitProbe().getTemperature()));
                else
                    pitTemp.setText("ERR");

                if(d.food1Probe().getTemperature() != 999)
                    food1Temp.setText(String.valueOf(d.food1Probe().getTemperature()));
                else
                    food1Temp.setText(getString(R.string.default_novalue));

                if(d.food2Probe().getTemperature() != 999)
                    food2Temp.setText(String.valueOf(d.food2Probe().getTemperature()));
                else
                    food2Temp.setText(getString(R.string.default_novalue));


                if(prefs.getBoolean(Preferences.DEBUG_DISTANCE, false))
                {
                    for(BleDevice bd : bleManager.getDevices_List())
                        if(bd.getMacAddress().equals(d.getAddress()))
                            debug.setText(bd.getDistance().toString());
                }
                else
                {
                    debug.setText("");
                }



                if (deviceManager.device().exceptions().hasException())
                {
                    for(DeviceExceptions.Exception e : deviceManager.device().exceptions().get())
                    {
                        switch(e)
                        {
                            case ENCLOSURE_HOT:
                                deviceName.setText("ENCLOSURE HOT");
                                deviceName.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case FOOD_1_PROBE_ERROR:
                                food1Temp.setText("ERR");
                                food1Temp.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case FOOD_2_PROBE_ERROR:
                                food2Temp.setText("ERR");
                                food2Temp.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case FOOD_1_DONE:
                                food1Temp.setAnimation(Animations.getBlinkAnimation());
                                deviceName.setText("Food 1 Done");
                                deviceName.setAnimation(Animations.getPulseAnimation(1000));
                                blinkRate = 250;
                                break;
                            case FOOD_2_DONE:
                                food2Temp.setAnimation(Animations.getBlinkAnimation());
                                deviceName.setText("Food 1 Done");
                                deviceName.setAnimation(Animations.getPulseAnimation(1000));
                                blinkRate = 250;
                                break;
                            case PIT_HOT:
                                pitTemp.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case PIT_COLD:
                                pitTemp.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case LID_OFF:
                                blinkRate = 250;
                                break;
                            case DELAY_PIT_SET:
                                blinkRate = 250;
                                break;
                            case FOOD_1_TEMP_PIT_SET:
                                blinkRate = 250;
                                break;
                            case FOOD_2_TEMP_PIT_SET:
                                blinkRate = 250;
                                break;
                            case PIT_PROBE_ERROR:
                                pitTemp.setText("ERR");
                                pitTemp.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case CONNECTION_LOST:
                                deviceName.setText("CONNECTION LOST");
                                blinkRate = 500;
                                break;
                        }
                    }
                }


            }
            else
            {
                deviceName.setText("Device Name");
                pitSet.setText("---");
                pitTemp.setText("---");
                food1Temp.setText("---");
                food2Temp.setText("---");

                food1Name.setText("Food 1");
                food2Name.setText("Food 2");
            }

            updateStatusIcon(statusIcon, blinkRate);

            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    }
}

