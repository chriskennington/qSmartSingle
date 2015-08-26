package com.pitmasteriq.qsmart;

import android.os.Handler;

/**
 * Created by Chris on 7/24/2015.
 */
public class UpdateThread extends Thread
{

    private boolean isRunning = true;
    private Handler handler;
    private Runnable runnable;
    private int sleepInterval;

    public UpdateThread(Handler handler, Runnable runnable, int sleepInterval)
    {
        this.handler = handler;
        this.runnable = runnable;
        this.sleepInterval = sleepInterval;
    }

    @Override
    public void run()
    {
        while(isRunning)
        {
            handler.post(runnable);

            try
            {
                sleep(sleepInterval);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setRunning(boolean running){this.isRunning = running;}
}
