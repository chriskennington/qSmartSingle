package com.pitmasteriq.qsmart;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.idevicesinc.sweetblue.BleManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment
{
    protected static final int UPDATE_INTERVAL = 2000;

    private Handler handler = new Handler();

    protected BleManager bleManager;

    protected DeviceManager deviceManager;
    protected Typeface seg7Font;

    protected SharedPreferences userPrefs;
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;

    public BaseFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bleManager = BleManager.get(getActivity().getApplicationContext());
        deviceManager = DeviceManager.get(getActivity().getApplicationContext());

        userPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefs = getActivity().getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        seg7Font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/segmental.ttf");
        return null;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        handler.post(new UpdateRunnable());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    protected void updateInterface(){};

    private class UpdateRunnable implements Runnable
    {
        @Override
        public void run()
        {
            updateInterface();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    }

    protected void updateStatusIcon(ImageView v, int blinkRate)
    {
        Device d = deviceManager.device();

        if (d != null)
        {
            switch (d.getStatus())
            {
                case Device.Status.OK:
                    v.setImageResource(R.drawable.status_icon_green);
                    break;
                case Device.Status.NoData:
                    v.setImageResource(R.drawable.status_icon_yellow);
                    break;
                case Device.Status.Disconnected:
                    v.setImageResource(R.drawable.status_icon_gray);
                    break;
                case Device.Status.LostConnection:
                case Device.Status.Unknown:
                    v.setImageResource(R.drawable.status_icon_red);
                    break;
            }

            if(d.exceptions().hasException())
            {
                if( blinkRate > 0 )
                    v.setAnimation(Animations.getBlinkAnimation(blinkRate));
            }
            else if (d.exceptions().hasSilenced())
            {
                if (blinkRate == 0)
                    blinkRate = 750;

                v.setAnimation(Animations.getPulseAnimation(blinkRate));
            }
            else
            {
                v.clearAnimation();
            }
        }
        else
            //device is null
            v.setImageResource(R.drawable.status_icon_gray);
    }
}
