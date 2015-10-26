package com.pitmasteriq.qsmart;

import android.content.Context;

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

    public int get()
    {
        if (!isFahrenheit())
            return f2c(temp);
        else
            return temp;
    }

    private int f2c(int f)
    {
        return (int) ((f-32) * (5.0/9.0));
    }

    private int c2f(int c)
    {
        return (int)(c * 1.8 +32);
    }

    private boolean isFahrenheit()
    {
        if (MyApplication.getAppContext().getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(Preferences.TEMPERATURE_UNITS, true))
            return true;
        else
            return false;
    }
}
