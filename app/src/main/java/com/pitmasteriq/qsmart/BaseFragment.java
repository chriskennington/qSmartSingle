package com.pitmasteriq.qsmart;


import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment
{
    protected static final int UPDATE_INTERVAL = 2000;

    private Handler handler = new Handler();

    protected Typeface seg7Font;

    public BaseFragment()
    {
        // Required empty public constructor
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
}
