package com.pitmasteriq.qsmartsingle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class AdvancedMonitorActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener
{


    //used for detecting swipe
    private float x1,x2, y1, y2;

    //*** UI COMPONENTS ********************
    ScrollView scrollView;

    TextView deviceName, food1ProbeName, food2ProbeName, deviceAddress, pitSet, pitTemp, food1Temp, food2Temp, pitAlarmLow, pitAlarmHigh, food1Alarm, food2Alarm, delayPitSet, delayTime,
            food1PitSet, atFood1Temp, food2PitSet, atFood2Temp;

    ImageView statusIcon;

    TableRow pitDeviationButton;

    RelativeLayout pitSetButton, food1AlarmButton, food2AlarmButton, delayPitSetButton, delayTimeButton,
            food1PitSetButton, food2PitSetButton, atFood1TempButton, atFood2TempButton;
    //**************************************


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_monitor);

        scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(this);


        deviceName = (TextView)findViewById(R.id.advanced_device_name);
        food1ProbeName = (TextView)findViewById(R.id.advanced_food_1_probe_name);
        food2ProbeName = (TextView)findViewById(R.id.advanced_food_2_probe_name);
        deviceAddress = (TextView)findViewById(R.id.advanced_device_address);
        pitSet = (TextView)findViewById(R.id.advanced_pit_set);
        pitTemp = (TextView)findViewById(R.id.advanced_pit_temp);
        food1Temp = (TextView)findViewById(R.id.advanced_food_1_temp);
        food2Temp  = (TextView)findViewById(R.id.advanced_food_2_temp);
        pitAlarmLow = (TextView)findViewById(R.id.advanced_pit_alarm_low);
        pitAlarmHigh = (TextView)findViewById(R.id.advanced_pit_alarm_high);
        food1Alarm = (TextView)findViewById(R.id.advanced_food_1_alarm);
        food2Alarm = (TextView)findViewById(R.id.advanced_food_2_alarm);
        delayPitSet = (TextView)findViewById(R.id.advanced_delay_pit_set);
        delayTime = (TextView)findViewById(R.id.advanced_delay_time);
        food1PitSet = (TextView)findViewById(R.id.advanced_food_1_pit_set);
        food2PitSet = (TextView)findViewById(R.id.advanced_food_2_pit_set);
        atFood1Temp = (TextView)findViewById(R.id.advanced_at_food_1_temp);
        atFood2Temp = (TextView)findViewById(R.id.advanced_at_food_2_temp);

        statusIcon = (ImageView)findViewById(R.id.advanced_status_icon);

        pitDeviationButton = (TableRow)findViewById(R.id.advanced_pit_deviation_button);

        pitSetButton = (RelativeLayout)findViewById(R.id.advanced_pit_set_button);
        food1AlarmButton = (RelativeLayout)findViewById(R.id.advanced_food_1_alarm_button);
        food2AlarmButton = (RelativeLayout)findViewById(R.id.advanced_food_2_alarm_button);
        delayPitSetButton = (RelativeLayout)findViewById(R.id.advanced_delay_pit_set_button);
        delayTimeButton = (RelativeLayout)findViewById(R.id.advanced_delay_time_button);
        food1PitSetButton = (RelativeLayout)findViewById(R.id.advanced_food_1_pit_set_button);
        food2PitSetButton = (RelativeLayout)findViewById(R.id.advanced_food_2_pit_set_button);
        atFood1TempButton = (RelativeLayout)findViewById(R.id.advanced_at_food_1_temp_button);
        atFood2TempButton = (RelativeLayout)findViewById(R.id.advanced_at_food_2_temp_button);

        deviceName.setOnClickListener(this);
        food1ProbeName.setOnClickListener(this);
        food2ProbeName.setOnClickListener(this);
        statusIcon.setOnClickListener(this);
        pitDeviationButton.setOnClickListener(this);
        pitSetButton.setOnClickListener(this);
        food1AlarmButton.setOnClickListener(this);
        food2AlarmButton.setOnClickListener(this);
        delayPitSetButton.setOnClickListener(this);
        delayTimeButton.setOnClickListener(this);
        food1PitSetButton.setOnClickListener(this);
        food2PitSetButton.setOnClickListener(this);
        atFood1TempButton.setOnClickListener(this);
        atFood2TempButton.setOnClickListener(this);


        pitSet.setTypeface(seg7Font);
        pitTemp.setTypeface(seg7Font);
        food1Temp.setTypeface(seg7Font);
        food2Temp.setTypeface(seg7Font);
        pitAlarmLow.setTypeface(seg7Font);
        pitAlarmHigh.setTypeface(seg7Font);
        food1Alarm.setTypeface(seg7Font);
        food2Alarm.setTypeface(seg7Font);
        delayPitSet.setTypeface(seg7Font);
        delayTime.setTypeface(seg7Font);
        food1PitSet.setTypeface(seg7Font);
        food2PitSet.setTypeface(seg7Font);
        atFood1Temp.setTypeface(seg7Font);
        atFood2Temp.setTypeface(seg7Font);


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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected Runnable getUiRunnable()
    {
        return new UpdateUiRunnable();
    }


    @Override
    public void onClick(View v)
    {
        Log.i("adv", "view clicked");

        if(deviceManager.device() != null)
        {
            switch (v.getId())
            {
                case R.id.advanced_device_name:
                    //openParameterDialog("Change Pit Set Temperature", deviceManager.device().getDefinedName(), Device.DEVICE_NAME);
                    break;
                case R.id.standard_food_1_name:
                    //openParameterDialog("Change Pit Set Temperature", deviceManager.device().food2Probe().getName(), Device.FOOD_1_PROBE_NAME);
                    break;
                case R.id.standard_food_2_name:
                    //openParameterDialog("Change Pit Set Temperature", deviceManager.device().food1Probe().getName(), Device.FOOD_2_PROBE_NAME);
                    break;
                case R.id.advanced_status_icon:
                    openExceptionFragment();
                    break;
                case R.id.advanced_pit_set_button:
                    openParameterDialog("Change Pit Set Temperature", deviceManager.device().config().getPitSet(), DeviceConfig.CONFIG_PIT_SET);
                    break;
                case R.id.advanced_pit_deviation_button:
                    openParameterDialog("Change Pit Temp Deviation", deviceManager.device().config().getPitAlarmDeviation(), DeviceConfig.CONFIG_PIT_ALARM);
                    break;
                case R.id.advanced_food_1_alarm_button:
                    openParameterDialog("Change Food 1 Alarm Temp", deviceManager.device().config().getFood1AlarmTemp(), DeviceConfig.CONFIG_FOOD_1_ALARM);
                    break;
                case R.id.advanced_food_2_alarm_button:
                    openParameterDialog("Change Food 2 Alarm Temp", deviceManager.device().config().getFood2AlarmTemp(), DeviceConfig.CONFIG_FOOD_2_ALARM);
                    break;
                case R.id.advanced_delay_pit_set_button:
                    openParameterDialog("Change Delay Pit Set", deviceManager.device().config().getDelayPitSet(), DeviceConfig.CONFIG_DELAY_PIT_SET);
                    break;
                case R.id.advanced_delay_time_button:
                    openParameterDialog("Change Delay Time", deviceManager.device().config().getDelayTime(), DeviceConfig.CONFIG_DELAY_TIME);
                    break;
                case R.id.advanced_food_1_pit_set_button:
                    openParameterDialog("Change Food 1 Pit Set", deviceManager.device().config().getFood1PitSet(), DeviceConfig.CONFIG_FOOD_1_PIT_SET);
                    break;
                case R.id.advanced_food_2_pit_set_button:
                    openParameterDialog("Change Food 2 Pit Set", deviceManager.device().config().getFood2PitSet(), DeviceConfig.CONFIG_FOOD_2_PIT_SET);
                    break;
                case R.id.advanced_at_food_1_temp_button:
                    openParameterDialog("Change Pit Set at Food 1 Temp", deviceManager.device().config().getFood1Temp(), DeviceConfig.CONFIG_FOOD_1_TEMP);
                    break;
                case R.id.advanced_at_food_2_temp_button:
                    openParameterDialog("Change Pit Set at Food 2 Temp", deviceManager.device().config().getFood2Temp(), DeviceConfig.CONFIG_FOOD_2_TEMP);
                    break;
                default:
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                float deltaX = Math.abs(x2 - x1);
                float deltaY = Math.abs(y2 - y1);


                if (deltaY < deltaX)
                {
                    if (deltaX > MIN_SWIPE_DISTANCE)
                    {
                        if (x2 > x1)
                        {
                            Toast.makeText(this, "swiped right", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, StandardMonitorActivity.class));
                            finish();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private class UpdateUiRunnable implements Runnable
    {
        @Override
        public void run()
        {
            if(ScannedDevices.get().hasNew())
                startScannerAnimation();

            int blinkRate = 0;

            Device d = deviceManager.device();
            if(d != null)
            {
                deviceName.clearAnimation();
                pitTemp.clearAnimation();
                food1Temp.clearAnimation();
                food2Temp.clearAnimation();
                pitAlarmHigh.clearAnimation();
                pitAlarmLow.clearAnimation();



                deviceName.setText(d.getDefinedName());
                deviceAddress.setText(d.getAddress());
                food1ProbeName.setText(d.food1Probe().getName());
                food2ProbeName.setText(d.food2Probe().getName());

                pitSet.setText(String.valueOf(d.config().getPitSet()));
                pitTemp.setText(String.valueOf(d.pitProbe().getTemperature()));

                if(d.food1Probe().getTemperature() != 999)
                    food1Temp.setText(String.valueOf(d.food1Probe().getTemperature()));
                else
                    food1Temp.setText(getString(R.string.default_novalue));

                if(d.food2Probe().getTemperature() != 999)
                    food2Temp.setText(String.valueOf(d.food2Probe().getTemperature()));
                else
                    food2Temp.setText(getString(R.string.default_novalue));


                if(d.config().getPitAlarmDeviation() > 0)
                {
                    pitAlarmLow.setText(String.valueOf(d.config().getPitSet() - d.config().getPitAlarmDeviation()));
                    pitAlarmHigh.setText(String.valueOf(d.config().getPitSet() + d.config().getPitAlarmDeviation()));
                }
                else
                {
                    pitAlarmLow.setText(getString(R.string.default_novalue));
                    pitAlarmHigh.setText(getString(R.string.default_novalue));
                }

                if(d.config().getFood1AlarmTemp() > 0)
                    food1Alarm.setText(String.valueOf(d.config().getFood1AlarmTemp()));
                else
                    food1Alarm.setText(getString(R.string.default_novalue));

                if(d.config().getFood2AlarmTemp() > 0)
                    food2Alarm.setText(String.valueOf(d.config().getFood2AlarmTemp()));
                else
                    food2Alarm.setText(getString(R.string.default_novalue));

                if(d.config().getFood1Temp() > 0 || d.config().getFood1PitSet() > 0)
                {
                    food1PitSet.setText(String.valueOf(d.config().getFood1PitSet()));
                    atFood1Temp.setText(String.valueOf(d.config().getFood1Temp()));
                }
                else
                {
                    food1PitSet.setText(getString(R.string.default_novalue));
                    atFood1Temp.setText(getString(R.string.default_novalue));
                }

                if(d.config().getFood2Temp() > 0 || d.config().getFood2PitSet() > 0)
                {
                    food2PitSet.setText(String.valueOf(d.config().getFood2PitSet()));
                    atFood2Temp.setText(String.valueOf(d.config().getFood2Temp()));
                }
                else
                {
                    food2PitSet.setText(getString(R.string.default_novalue));
                    atFood2Temp.setText(getString(R.string.default_novalue));
                }

                if(d.config().getDelayPitSet() > 0 || d.config().getDelayTime() > 0)
                {
                    delayPitSet.setText(String.valueOf(d.config().getDelayPitSet()));

                    int minutes = d.config().getDelayTime() * 15;
                    int hours = minutes / 60;
                    minutes = (minutes - hours * 60) ;
                    String m = "00";
                    String h = "00";

                    if(minutes == 0)
                        m = "00";
                    else
                        m = String.valueOf(minutes);

                    if(hours == 0)
                        h = "00";
                    else if (hours < 10)
                        h = "0" + hours;
                    else
                        h = String.valueOf(hours);

                    delayTime.setText(h + ":" + m);
                }
                else
                {
                    delayTime.setText(getString(R.string.default_novalue));
                    delayPitSet.setText(getString(R.string.default_novalue));
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
                                food2Temp.setAnimation(Animations.getBlinkAnimation());
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
                                deviceName.setText("Food 2 Done");
                                deviceName.setAnimation(Animations.getPulseAnimation(1000));
                                blinkRate = 250;
                                break;
                            case PIT_HOT:
                                pitTemp.setAnimation(Animations.getBlinkAnimation());
                                pitAlarmHigh.setAnimation(Animations.getBlinkAnimation());
                                blinkRate = 250;
                                break;
                            case PIT_COLD:
                                pitTemp.setAnimation(Animations.getBlinkAnimation());
                                pitAlarmLow.setAnimation(Animations.getBlinkAnimation());
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
                deviceName.setText(getString(R.string.default_novalue));
                food1ProbeName.setText(getString(R.string.default_novalue));
                food2ProbeName.setText(getString(R.string.default_novalue));
                pitSet.setText(getString(R.string.default_novalue));
                pitTemp.setText(getString(R.string.default_novalue));
                food1Temp.setText(getString(R.string.default_novalue));
                food2Temp.setText(getString(R.string.default_novalue));
                pitAlarmLow.setText(getString(R.string.default_novalue));
                pitAlarmHigh.setText(getString(R.string.default_novalue));
                food1Alarm.setText(getString(R.string.default_novalue));
                food2Alarm.setText(getString(R.string.default_novalue));
                delayPitSet.setText(getString(R.string.default_novalue));
                delayTime.setText(getString(R.string.default_novalue));
                food1PitSet.setText(getString(R.string.default_novalue));
                food2PitSet.setText(getString(R.string.default_novalue));
                atFood1Temp.setText(getString(R.string.default_novalue));
                atFood2Temp.setText(getString(R.string.default_novalue));
            }

            updateStatusIcon(statusIcon, blinkRate);

            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    }
}
