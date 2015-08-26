package com.pitmasteriq.qsmart;

import android.util.Log;

import com.idevicesinc.sweetblue.BleManager;

/**
 * Created by Chris on 8/21/2015.
 */
public class MyDiscoveryListener implements BleManager.DiscoveryListener
{
    @Override
    public void onEvent(DiscoveryEvent e)
    {
        if (e.lifeCycle() == LifeCycle.DISCOVERED)
            Log.e("debug", "discovered " + e.device().getMacAddress());

        if (e.lifeCycle() == LifeCycle.REDISCOVERED)
            Log.e("debug", "rediscovered " + e.device().getMacAddress());

        if (e.lifeCycle() == LifeCycle.UNDISCOVERED)
            Log.e("debug", "undiscovered " + e.device().getMacAddress());
    }
}
