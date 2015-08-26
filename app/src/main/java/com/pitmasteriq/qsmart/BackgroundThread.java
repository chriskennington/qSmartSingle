package com.pitmasteriq.qsmart;


public class BackgroundThread extends Thread
{
    private Runnable runnable;

    public BackgroundThread(Runnable runnable)
    {
        this.runnable = runnable;
    }

    @Override
    public void run()
    {
        runnable.run();
    }
}
