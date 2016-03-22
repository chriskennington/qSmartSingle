package com.pitmasteriq.qsmart;

import android.util.Log;

/**
 * Created by Chris on 3/9/2016.
 */
public class Console
{
    private static final String DEFAULT_TAG = "pmiq";

    public static void v (String message)
    {
        Log.v(DEFAULT_TAG, message);
    }

    public static void d (String message)
    {
        Log.d(DEFAULT_TAG, message);
    }

    public static void i (String message)
    {
        Log.i(DEFAULT_TAG, message);
    }

    public static void w (String message)
    {
        Log.w(DEFAULT_TAG, message);
    }

    public static void e (String message)
    {
        Log.e(DEFAULT_TAG, message);
    }
}
