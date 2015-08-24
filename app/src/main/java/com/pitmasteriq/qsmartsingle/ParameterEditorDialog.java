package com.pitmasteriq.qsmartsingle;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Created by Chris on 5/11/2015.
 */
public class ParameterEditorDialog extends DialogFragment
{
    private ParameterEditorListener listener;
    private Intent intent;

    //*** SharedPrefence Objects ****
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;
    //*******************************

    public ParameterEditorDialog(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        prefs = getActivity().getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        prefEditor = prefs.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_parameter_editor, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);


        TextView titleView = (TextView)v.findViewById(R.id.delay_time_editor_title);
        final EditText value = (EditText)v.findViewById(R.id.parameter_editor_input);
        Button cancel = (Button)v.findViewById(R.id.parameter_editor_cancel);
        Button ok = (Button)v.findViewById(R.id.parameter_editor_ok);

        final int selector  = getArguments().getInt("selector", -1);


        titleView.setText(getArguments().getString("title"));
        value.setHint("Current value: " + getArguments().getInt("current"));

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDialog().dismiss();
            }

        });

        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                intent = getActivity().getIntent();


                if( checkValue(value.getText().toString(), selector ))
                {
                    listener.parameterChanged(Activity.RESULT_OK, intent);
                }
                else
                {
                    listener.parameterChanged(Activity.RESULT_CANCELED, intent);
                }

                getDialog().dismiss();
            }

        });

        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (ParameterEditorListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    public interface ParameterEditorListener
    {
        void parameterChanged(int resultCode, Intent data);
    }

    public boolean checkValue(String result, int selector)
    {
        if(result != null && result.length() > 0)
        {
            int tempValue = Integer.parseInt(result);
            switch(selector)
            {
                case DeviceConfig.CONFIG_PIT_SET:
                    if(tempValue < 150 || tempValue > 400)
                    {
                        intent.putExtra("min", 150);
                        intent.putExtra("max", 400);
                        return false;
                    }
                    break;
                case DeviceConfig.CONFIG_FOOD_1_ALARM:
                case DeviceConfig.CONFIG_FOOD_2_ALARM:
                case DeviceConfig.CONFIG_FOOD_1_TEMP:
                case DeviceConfig.CONFIG_FOOD_2_TEMP:
                    if( (tempValue < 50 || tempValue > 250) && tempValue != 0 )
                    {
                        intent.putExtra("min", 50);
                        intent.putExtra("max", 250);
                        return false;
                    }
                    break;

                case DeviceConfig.CONFIG_PIT_ALARM:
                    if( (tempValue < 20 || tempValue > 100) && tempValue != 0 )
                    {
                        intent.putExtra("min", 20);
                        intent.putExtra("max", 100);
                        return false;
                    }
                    break;

                case DeviceConfig.CONFIG_DELAY_PIT_SET:
                case DeviceConfig.CONFIG_FOOD_1_PIT_SET:
                case DeviceConfig.CONFIG_FOOD_2_PIT_SET:
                    if( (tempValue < 150 || tempValue > 400) && tempValue != 0 )
                    {
                        intent.putExtra("min", 150);
                        intent.putExtra("max", 400);
                        return false;
                    }
                    break;

                case DeviceConfig.CONFIG_DELAY_TIME:
                    if( (tempValue < 0 || tempValue > 96))
                    {
                        intent.putExtra("min", 0);
                        intent.putExtra("max", 96);
                        return false;
                    }
                    break;
            }

            intent.putExtra("value", tempValue);
            intent.putExtra("selector", selector);
            return true;
        }
        return false;
    }

}
