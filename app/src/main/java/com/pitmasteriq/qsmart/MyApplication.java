package com.pitmasteriq.qsmart;

import android.app.Application;
import android.content.Context;

import org.acra.annotation.ReportsCrashes;

@ReportsCrashes
        (
                mailTo = "chris@pitmasteriq.com"
        )
public class MyApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();

        context = getApplicationContext();

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
    }

    public static Context getAppContext()
    {
        return context;
    }
}
