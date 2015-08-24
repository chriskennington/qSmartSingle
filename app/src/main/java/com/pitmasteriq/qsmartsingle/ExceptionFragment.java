package com.pitmasteriq.qsmartsingle;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 8/3/2015.
 */
public class ExceptionFragment extends DialogFragment
{

    private DeviceManager deviceManager;

    private Dialog dialog;

    private List<CheckBox> checkboxes = new ArrayList<>();

    private Context context;

    private Map<CheckBox, DeviceExceptions.Exception> map   = new HashMap<>();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.context = getActivity();

        deviceManager = DeviceManager.get(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_exception, container, false);

        dialog = this.getDialog();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        TextView title = (TextView)v.findViewById(R.id.error_title);
        LinearLayout exceptions = (LinearLayout) v.findViewById(R.id.error_layout_exceptions);
        LinearLayout sExceptions = (LinearLayout) v.findViewById(R.id.error_layout_silenced_exceptions);
        Button cancel = (Button) v.findViewById(R.id.error_cancel);
        Button ok = (Button) v.findViewById(R.id.error_ok);


        title.setText(deviceManager.device().getDefinedName() + " Exceptions");

        cancel.setOnClickListener(cancelListener);
        ok.setOnClickListener(okListener);

        addExceptions(exceptions);
        addSilencedExceptions(sExceptions);

        return v;
    }




    private void addExceptions(LinearLayout layout)
    {
        HashSet<DeviceExceptions.Exception> exceptions = deviceManager.device().exceptions().get();

        if(exceptions.size() > 0)
        {
            for(DeviceExceptions.Exception e : exceptions)
            {
                Log.i("ADD FLAG", "ADDING FLAG TO FRAGMENT");
                CheckBox temp = new CheckBox(context);
                temp.setText( e.name() );
                checkboxes.add(temp);
                temp.setTextColor(Color.WHITE);
                map.put(temp, e);

                layout.addView(temp);
            }
        }
        else
        {
            TextView temp = new TextView(context);
            temp.setText(R.string.no_exceptions_found);
            temp.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 25;
            lp.bottomMargin = 25;
            temp.setLayoutParams(lp);
            temp.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(temp);
        }
    }

    private void addSilencedExceptions(LinearLayout layout)
    {
        HashSet<DeviceExceptions.Exception> exceptions = deviceManager.device().exceptions().silenced();

        if (exceptions.size() > 0)
        {
            for (DeviceExceptions.Exception e : exceptions)
            {
                TextView temp = new TextView(context);
                temp.setText(e.name());
                temp.setTextColor(Color.WHITE);
                layout.addView(temp);
            }
        } else
        {
            TextView temp = new TextView(context);
            temp.setText(R.string.no_silenced_exceptions);
            temp.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 25;
            lp.bottomMargin = 25;
            temp.setLayoutParams(lp);
            temp.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(temp);
        }
    }

    private View.OnClickListener cancelListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            dialog.dismiss();
        }
    };

    private View.OnClickListener okListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            for(CheckBox cb : checkboxes)
            {
                if(cb.isChecked())
                {
                    //TODO silence exception
                    deviceManager.device().exceptions().silenceException(map.get(cb));
                }
            }
            dialog.dismiss();
        }
    };

}
