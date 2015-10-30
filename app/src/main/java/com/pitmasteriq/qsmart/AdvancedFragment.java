package com.pitmasteriq.qsmart;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Formatter;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedFragment extends BaseFragment
{
    private static AdvancedFragment instance;

    //*** UI COMPONENTS ********************
    ScrollView scrollView;

    TextView deviceName, food1ProbeName, food2ProbeName, deviceAddress, pitSet, pitTemp, food1Temp, food2Temp, pitAlarmLow, pitAlarmHigh, food1Alarm, food2Alarm, delayPitSet, delayTime,
            food1PitSet, atFood1Temp, food2PitSet, atFood2Temp, blowerPower;

    ImageView statusIcon;

    TableRow pitDeviationButton;

    RelativeLayout pitSetButton, food1AlarmButton, food2AlarmButton, delayPitSetButton, delayTimeButton,
            food1PitSetButton, food2PitSetButton, atFood1TempButton, atFood2TempButton;
    //**************************************



    private int counter = 10;

    public AdvancedFragment()
    {
        // Required empty public constructor
    }

    public static AdvancedFragment newInstance()
    {
        if (instance == null)
            instance = new AdvancedFragment();
        return instance;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_advanced, container, false);

        deviceName = (TextView)v.findViewById(R.id.advanced_device_name);
        food1ProbeName = (TextView)v.findViewById(R.id.advanced_food_1_probe_name);
        food2ProbeName = (TextView)v.findViewById(R.id.advanced_food_2_probe_name);
        deviceAddress = (TextView)v.findViewById(R.id.advanced_device_address);
        pitSet = (TextView)v.findViewById(R.id.advanced_pit_set);
        pitTemp = (TextView)v.findViewById(R.id.advanced_pit_temp);
        food1Temp = (TextView)v.findViewById(R.id.advanced_food_1_temp);
        food2Temp  = (TextView)v.findViewById(R.id.advanced_food_2_temp);
        pitAlarmLow = (TextView)v.findViewById(R.id.advanced_pit_alarm_low);
        pitAlarmHigh = (TextView)v.findViewById(R.id.advanced_pit_alarm_high);
        food1Alarm = (TextView)v.findViewById(R.id.advanced_food_1_alarm);
        food2Alarm = (TextView)v.findViewById(R.id.advanced_food_2_alarm);
        delayPitSet = (TextView)v.findViewById(R.id.advanced_delay_pit_set);
        delayTime = (TextView)v.findViewById(R.id.advanced_delay_time);
        food1PitSet = (TextView)v.findViewById(R.id.advanced_food_1_pit_set);
        food2PitSet = (TextView)v.findViewById(R.id.advanced_food_2_pit_set);
        atFood1Temp = (TextView)v.findViewById(R.id.advanced_at_food_1_temp);
        atFood2Temp = (TextView)v.findViewById(R.id.advanced_at_food_2_temp);
        blowerPower = (TextView)v.findViewById(R.id.advanced_blower_power);

        statusIcon = (ImageView)v.findViewById(R.id.advanced_status_icon);

        pitDeviationButton = (TableRow)v.findViewById(R.id.advanced_pit_deviation_button);

        pitSetButton = (RelativeLayout)v.findViewById(R.id.advanced_pit_set_button);
        food1AlarmButton = (RelativeLayout)v.findViewById(R.id.advanced_food_1_alarm_button);
        food2AlarmButton = (RelativeLayout)v.findViewById(R.id.advanced_food_2_alarm_button);
        delayPitSetButton = (RelativeLayout)v.findViewById(R.id.advanced_delay_pit_set_button);
        delayTimeButton = (RelativeLayout)v.findViewById(R.id.advanced_delay_time_button);
        food1PitSetButton = (RelativeLayout)v.findViewById(R.id.advanced_food_1_pit_set_button);
        food2PitSetButton = (RelativeLayout)v.findViewById(R.id.advanced_food_2_pit_set_button);
        atFood1TempButton = (RelativeLayout)v.findViewById(R.id.advanced_at_food_1_temp_button);
        atFood2TempButton = (RelativeLayout)v.findViewById(R.id.advanced_at_food_2_temp_button);

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
        blowerPower.setTypeface(seg7Font);

        return v;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    protected void updateInterface()
    {
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
            blowerPower.setText(String.valueOf(d.getBlowerPower()));

            pitSet.setText(String.valueOf(d.config().pitSet().get()));
            pitTemp.setText(String.valueOf(d.pitProbe().temperature()));

            if(d.pitProbe().temperature().getRawTemp() != 999)
                pitTemp.setText(String.valueOf(d.pitProbe().temperature().get()));
            else
                pitTemp.setText("ERR");

            if(d.food1Probe().temperature().getRawTemp() != 999)
                food1Temp.setText(String.valueOf(d.food1Probe().temperature().get()));
            else
                food1Temp.setText(getString(R.string.default_novalue));

            if(d.food2Probe().temperature().getRawTemp() != 999)
                food2Temp.setText(String.valueOf(d.food2Probe().temperature().get()));
            else
                food2Temp.setText(getString(R.string.default_novalue));


            if(d.config().pitAlarmDeviation().getRawTemp() > 0)
            {
                pitAlarmLow.setText(String.valueOf(d.config().pitSet().get() - d.config().pitAlarmDeviation().getRelative()));
                pitAlarmHigh.setText(String.valueOf(d.config().pitSet().get() + d.config().pitAlarmDeviation().getRelative()));
            }
            else
            {
                pitAlarmLow.setText(getString(R.string.default_novalue));
                pitAlarmHigh.setText(getString(R.string.default_novalue));
            }

            if(d.config().food1AlarmTemp().getRawTemp() > 0)
                food1Alarm.setText(String.valueOf(d.config().food1AlarmTemp().get()));
            else
                food1Alarm.setText(getString(R.string.default_novalue));

            if(d.config().food2AlarmTemp().getRawTemp() > 0)
                food2Alarm.setText(String.valueOf(d.config().food2AlarmTemp().get()));
            else
                food2Alarm.setText(getString(R.string.default_novalue));


            if(d.config().food1Temp().getRawTemp() > 0 || d.config().food1PitSet().getRawTemp() > 0)
            {
                food1PitSet.setText(String.valueOf(d.config().food1PitSet().get()));
                atFood1Temp.setText(String.valueOf(d.config().food1Temp().get()));
            }
            else
            {
                food1PitSet.setText(getString(R.string.default_novalue));
                atFood1Temp.setText(getString(R.string.default_novalue));
            }

            if(d.config().food2Temp().getRawTemp() > 0 || d.config().food2PitSet().getRawTemp() > 0)
            {
                food2PitSet.setText(String.valueOf(d.config().food2PitSet().get()));
                atFood2Temp.setText(String.valueOf(d.config().food2Temp().get()));
            }
            else
            {
                food2PitSet.setText(getString(R.string.default_novalue));
                atFood2Temp.setText(getString(R.string.default_novalue));
            }

            if(d.config().delayPitSet().getRawTemp() > 0 || d.config().getDelayTime() > 0)
            {
                delayPitSet.setText(String.valueOf(d.config().delayPitSet().get()));

                int minutes = d.config().getDelayTime() * 15;
                minutes -= d.config().getMinutesPast();
                int hours = minutes / 60;
                minutes = (minutes - hours * 60) ;

                if(minutes < 0)
                    minutes = 0;

                Formatter formatter = new Formatter();
                String h = formatter.format("%02d", hours).toString();
                formatter.close();

                formatter = new Formatter();
                String m = formatter.format("%02d", minutes).toString();
                formatter.close();

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
            deviceName.setText("Device Name");
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
    }
}
