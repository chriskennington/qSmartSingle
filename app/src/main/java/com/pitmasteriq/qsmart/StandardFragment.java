package com.pitmasteriq.qsmart;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


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
        counter++;

        deviceName.setText(""+counter);
    }
}
