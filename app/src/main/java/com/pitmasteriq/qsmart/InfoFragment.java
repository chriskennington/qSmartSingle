package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Chris on 5/14/2015.
 */
public class InfoFragment extends DialogFragment
{
    private Context context;
    private FragmentResponseListener listener;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;


    public InfoFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context = getActivity();

        prefs = context.getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_info, container, false);

        TextView appVersion = (TextView) v.findViewById(R.id.info_qsmart_version);
        TextView androidVersion = (TextView) v.findViewById(R.id.info_android_version);
        TextView manuf = (TextView) v.findViewById(R.id.info_device_manufacturer);
        TextView model = (TextView) v.findViewById(R.id.info_device_model);

        String v1 = getArguments().getString("app_version");
        String v2 = getArguments().getString("android_version");
        String m1 = getArguments().getString("manuf");
        String m2 = getArguments().getString("model");

        final Dialog dialog = this.getDialog();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        appVersion.setText("qSmart Version: " + v1);
        androidVersion.setText("Android Version: " + v2);
        manuf.setText("Manufacturer: " + m1);
        model.setText("Device Model: " + m2);

        Button close = (Button)v.findViewById(R.id.info_close);
        Button exit = (Button)v.findViewById(R.id.info_exit);

        close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        exit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onFragmentResponse(new FragmentResponseEvent(FragmentResponseEvent.Type.CLOSE));
                dialog.dismiss();
            }
        });

        appVersion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(prefs.getBoolean(Preferences.DEBUG_DISTANCE, false))
                {
                    editor.putBoolean(Preferences.DEBUG_DISTANCE, false).commit();
                }
                else
                    editor.putBoolean(Preferences.DEBUG_DISTANCE, true).commit();
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
            listener = (FragmentResponseListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement InfoFragmentListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
