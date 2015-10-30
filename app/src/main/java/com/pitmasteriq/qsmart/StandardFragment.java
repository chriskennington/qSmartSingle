package com.pitmasteriq.qsmart;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleDevice;


/**
 * A simple {@link Fragment} subclass.
 */
public class StandardFragment extends BaseFragment
{

    //*** UI COMPONENTS ***
    private TextView deviceName, food1Name, food2Name, pitSet, pitTemp, food1Temp, food2Temp, debug;
    private ImageView statusIcon;
    private RelativeLayout pitSetClickLoc, food1ClickLoc, food2ClickLoc;
    //*********************

    private static StandardFragment instance;

    private int counter = 0;

    public StandardFragment(){}

    public static StandardFragment newInstance()
    {
        if (instance == null)
            instance = new StandardFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_standard, container, false);

        deviceName = (TextView)v.findViewById(R.id.standard_device_name);
        food1Name = (TextView)v.findViewById(R.id.standard_food_1_name);
        food2Name = (TextView)v.findViewById(R.id.standard_food_2_name);
        pitSet = (TextView)v.findViewById(R.id.standard_pit_set_value);
        pitTemp = (TextView)v.findViewById(R.id.standard_pit_temp_value);
        food1Temp = (TextView)v.findViewById(R.id.standard_food_1_temp);
        food2Temp = (TextView)v.findViewById(R.id.standard_food_2_temp);
        debug = (TextView)v.findViewById(R.id.standard_debug);
        statusIcon = (ImageView)v.findViewById(R.id.standard_status_icon);

        food1ClickLoc = (RelativeLayout)v.findViewById(R.id.standard_food_1_click_loc);
        food2ClickLoc = (RelativeLayout)v.findViewById(R.id.standard_food_2_click_loc);
        pitSetClickLoc = (RelativeLayout)v.findViewById(R.id.standard_pit_set_click_loc);

        pitSet.setTypeface(seg7Font);
        pitTemp.setTypeface(seg7Font);
        food1Temp.setTypeface(seg7Font);
        food2Temp.setTypeface(seg7Font);

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


            deviceName.setText(d.getDefinedName());
            food1Name.setText(d.food1Probe().getName());
            food2Name.setText(d.food2Probe().getName());



            pitSet.setText(String.valueOf(d.config().pitSet().get()));

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
    }
}
