package com.pitmasteriq.qsmart;

import android.preference.PreferenceManager;

/**
 * Created by Chris on 10/26/2015.
 */
public class Temperature
{
    private int temp = 0;

    public void set(int temp){this.temp = temp;}

    /*public void set(int temp)
    {
        if (!isFahrenheit())
            this.temp = c2f(temp);
        else
            this.temp = temp;
    }*/

    public int getRawTemp()
    {
        return temp;
    }

    public int get()
    {
        if (!isFahrenheit())
            return f2c(temp);
        else
            return temp;
    }

    public int getRelative()
    {
        if (!isFahrenheit())
            return f2cr(temp);
        else
            return temp;
    }

    public static int f2c(int f)
    {
        return (int) ((f-32) * (5.0/9.0));
    }

    public static int c2f(int c)
    {
        return (int)(c * 1.8 +32);
    }

    private int f2cr(int f)
    {
        return (int) ((5.0/9.0) * f);
    }

    private boolean isFahrenheit()
    {
        if (PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext())
                .getBoolean(Preferences.TEMPERATURE_UNITS, true))
            return true;
        else
            return false;
    }
}
