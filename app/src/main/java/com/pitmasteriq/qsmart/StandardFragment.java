package com.pitmasteriq.qsmart;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleDevice;
import com.pitmasteriq.qsmart.exception.ExceptionHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class StandardFragment extends BaseFragment
{

    //*** UI COMPONENTS ***
    private TextView deviceName, food1Name, food2Name, pitSet, pitTemp, food1Temp, food2Temp, debug;
    private ImageView statusIcon;
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
        Device d = null;
        try
        {
           d = deviceManager.device();
        } catch (NullDeviceException e) {}

        if(deviceManager.hasDevice())
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
                pitTemp.setText(R.string.display_error);

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

            ExceptionHelper eHelper = ExceptionHelper.get();

            if (eHelper.hasActiveExceptions())
            {
                for(com.pitmasteriq.qsmart.exception.Exception e : eHelper.getActiveExceptions())
                {
                    switch(e.getId())
                    {
                        case 0:
                            deviceName.setText(R.string.enclosure_hot);
                            deviceName.setAnimation(Animations.getBlinkAnimation());
                            break;
                        case 1:
                            food1Temp.setText(R.string.display_error);
                            food1Temp.setAnimation(Animations.getBlinkAnimation());
                            break;
                        case 2:
                            food2Temp.setText(R.string.display_error);
                            food2Temp.setAnimation(Animations.getBlinkAnimation());
                            break;
                        case 3:
                            food1Temp.setAnimation(Animations.getBlinkAnimation());
                            deviceName.setText(R.string.food_1_done_msg);
                            deviceName.setAnimation(Animations.getPulseAnimation(1000));
                            break;
                        case 4:
                            food2Temp.setAnimation(Animations.getBlinkAnimation());
                            deviceName.setText(R.string.food_2_done_msg);
                            deviceName.setAnimation(Animations.getPulseAnimation(1000));
                            break;
                        case 5:
                            pitTemp.setAnimation(Animations.getBlinkAnimation());
                            break;
                        case 6:
                            pitTemp.setAnimation(Animations.getBlinkAnimation());
                            break;
                        case 11: //pit probe error
                            pitTemp.setText(R.string.display_error);
                            pitTemp.setAnimation(Animations.getBlinkAnimation());
                            break;
                        case 14: //connection lost
                            deviceName.setText(R.string.connection_lost_msg);
                            break;
                    }
                }
            }
        }
        else
        {
            deviceName.setText(R.string.device_name_label);
            pitSet.setText(R.string.default_novalue);
            pitTemp.setText(R.string.default_novalue);
            food1Temp.setText(R.string.default_novalue);
            food2Temp.setText(R.string.default_novalue);

            food1Name.setText(R.string.food1_probe_name);
            food2Name.setText(R.string.food2_probe_name);
        }

        updateStatusIcon(statusIcon);
    }
}
