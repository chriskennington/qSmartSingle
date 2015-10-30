package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
    private ParameterEditedListener listener;
    private Intent intent;

    //*** SharedPrefence Objects ****
    private SharedPreferences userPrefs;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;
    //*******************************

    private boolean fahrenheit;

    public ParameterEditorDialog(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        userPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefs = getActivity().getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        prefEditor = prefs.edit();

        fahrenheit = userPrefs.getBoolean(Preferences.TEMPERATURE_UNITS, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_parameter_editor, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);


        TextView titleView = (TextView)v.findViewById(R.id.text_editor_title);
        final EditText value = (EditText)v.findViewById(R.id.parameter_editor_input);
        Button cancel = (Button)v.findViewById(R.id.parameter_editor_cancel);
        Button ok = (Button)v.findViewById(R.id.parameter_editor_ok);

        final int selector  = getArguments().getInt("selector", -1);
        String unitDisplay;

        if (fahrenheit)
            unitDisplay = "°F";
        else
            unitDisplay = "°C";

        titleView.setText(getArguments().getString("title"));
        value.setHint("Current value: " + getArguments().getInt("current") + " " + unitDisplay);

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
                    listener.onParameterChanged(new ParameterEditedEvent(Activity.RESULT_OK, intent));
                }
                else
                {
                    listener.onParameterChanged(new ParameterEditedEvent(Activity.RESULT_CANCELED, intent));
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
            listener = (ParameterEditedListener) activity;
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

    public boolean checkValue(String result, int selector)
    {
        if(result != null && result.length() > 0)
        {
            int tempValue = Integer.parseInt(result);

            Log.e("TAG", "entered value " + tempValue);

            if (!fahrenheit)
            {
                tempValue = c2fa(tempValue);
            }

            Log.e("TAG", "converted value " + tempValue);

            switch(selector)
            {
                case DeviceConfig.CONFIG_PIT_SET:
                    if(tempValue < 150 || tempValue > 400)
                    {
                        if (fahrenheit)
                        {
                            intent.putExtra("min", 150);
                            intent.putExtra("max", 400);
                        }
                        else
                        {
                            intent.putExtra("min", 66);
                            intent.putExtra("max", 204);
                        }
                        return false;
                    }
                    break;
                case DeviceConfig.CONFIG_FOOD_1_ALARM:
                case DeviceConfig.CONFIG_FOOD_2_ALARM:
                case DeviceConfig.CONFIG_FOOD_1_TEMP:
                case DeviceConfig.CONFIG_FOOD_2_TEMP:
                    if( (tempValue < 100 || tempValue > 250) && tempValue != 0 )
                    {
                        if (fahrenheit)
                        {
                            intent.putExtra("min", 100);
                            intent.putExtra("max", 250);
                        }
                        else
                        {
                            intent.putExtra("min", 10);
                            intent.putExtra("max", 121);
                        }
                        return false;
                    }
                    break;

                case DeviceConfig.CONFIG_PIT_ALARM:
                    if( (tempValue < 20 || tempValue > 100) && tempValue != 0 )
                    {
                        if (fahrenheit)
                        {
                            intent.putExtra("min", 20);
                            intent.putExtra("max", 100);
                        }
                        else
                        {
                            intent.putExtra("min", 11);
                            intent.putExtra("max", 56);
                        }
                        return false;
                    }
                    break;

                case DeviceConfig.CONFIG_DELAY_PIT_SET:
                case DeviceConfig.CONFIG_FOOD_1_PIT_SET:
                case DeviceConfig.CONFIG_FOOD_2_PIT_SET:
                    if( (tempValue < 150 || tempValue > 400) && tempValue != 0 )
                    {
                        if (fahrenheit)
                        {
                            intent.putExtra("min", 150);
                            intent.putExtra("max", 400);
                        }
                        else
                        {
                            intent.putExtra("min", 66);
                            intent.putExtra("max", 204);
                        }
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

    private int c2fa(int c)
    {
        return (int) (((9.0/5.0) * c) + 32 );
    }
}
