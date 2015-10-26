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
        counter++;

        deviceName.setText(""+counter);
    }
}
