package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Formatter;


/**
 * Created by Chris on 8/21/2015.
 */
public class DelayTimeEditorDialog extends DialogFragment
{
    private ParameterEditorDialog.ParameterEditorListener listener;
    private Intent intent;

    private int newTime = 0;

    //*** SharedPrefence Objects ****
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;
    //*******************************

    private int current, selector;

    public DelayTimeEditorDialog(){}

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
        View v = inflater.inflate(R.layout.fragment_delay_time_editor, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);



        TextView title = (TextView) v.findViewById(R.id.delay_time_editor_title);
        final EditText time = (EditText) v.findViewById(R.id.delay_time_editor_time);
        Button less = (Button) v.findViewById(R.id.delay_time_editor_down);
        Button more = (Button) v.findViewById(R.id.delay_time_editor_up);
        Button cancel = (Button)v.findViewById(R.id.delay_time_editor_cancel);
        Button ok = (Button)v.findViewById(R.id.delay_time_editor_ok);

        selector = getArguments().getInt("selector", -1);
        current = getArguments().getInt("current", 0);

        title.setText(getArguments().getString("title"));

        time.setText(calculateTime(current));
        newTime = current;


        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDialog().dismiss();
            }

        });

        less.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (newTime > 0)
                {
                    newTime--;
                    time.setText(calculateTime(-1));
                }
            }
        });

        more.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (newTime < 96)
                {
                    newTime++;
                    time.setText(calculateTime(-1));
                }
            }
        });

        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                intent = getActivity().getIntent();

                if( checkValue() )
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

    private boolean checkValue()
    {
        if( (newTime < 0 || newTime > 96))
        {
            intent.putExtra("min", 0);
            intent.putExtra("max", 96);
            return false;
        }

        intent.putExtra("value", newTime);
        intent.putExtra("selector", selector);
        return true;
    }

    private String calculateTime(int i)
    {
        if(i == -1)
            i = newTime;
        int minutes = i * 15;
        int hours = minutes / 60;

        Log.e("TAG" ,"minutes 1 " + minutes);
        Log.e("TAG", "hours 1 " + hours);

        minutes = minutes - (hours * 60);

        Formatter formatter = new Formatter();
        String m = formatter.format("%02d", minutes).toString();
        formatter.close();

        formatter = new Formatter();
        String h = formatter.format("%02d", hours).toString();
        formatter.close();

        Log.e("TAG" ,"minutes 2 " +  m);
        Log.e("TAG" ,"hours 2 " + h);

        return h + ":" + m;

    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (ParameterEditorDialog.ParameterEditorListener) activity;
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

}
