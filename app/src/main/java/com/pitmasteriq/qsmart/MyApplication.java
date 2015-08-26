package com.pitmasteriq.qsmart;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes
        (
                mailTo = "chris@pitmasteriq.com"
        )
public class MyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();


        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
