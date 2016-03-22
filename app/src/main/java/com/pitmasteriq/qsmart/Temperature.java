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
        if (temp == 0)
            return 0;

        if (!isFahrenheit())
            return f2c(temp);
        else
            return temp;
    }

    public int getRelative()
    {
        if (temp == 0)
            return 0;

        if (!isFahrenheit())
            return f2cr(temp);
        else
            return temp;
    }

    public static int f2c(int f)
    {
        float temp = (float) ((f-32) * (5.0/9.0));
        return roundUpOrDown(temp);
    }

    public static int c2fa(int c)
    {
        float temp = (float)(c * 1.8 +32);
        return roundUpOrDown(temp);
    }

    public static int c2fr(int c)
    {
        float temp = (float) (c * 1.8);
        return roundUpOrDownRelative(c, temp);
    }

    private int f2cr(int f)
    {
        float temp = (float) ((5.0/9.0) * f);
        return roundUpOrDown(temp);
    }

    private boolean isFahrenheit()
    {
        if (PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext())
                .getBoolean(Preferences.TEMPERATURE_UNITS, true))
            return true;
        else
            return false;
    }

    private static int roundUpOrDown(float temp)
    {
        if (temp > 0)
            temp += 0.5;
        else if (temp < 0)
            temp -= 0.5;

        return (int) temp;
    }

    private static int roundUpOrDownRelative(int c, float temp)
    {
        switch (c)
        {
            case 11: case 12: case 16: case 17: case 21:
            case 22: case 26: case 27: case 31: case 32:
            case 36: case 37: case 41: case 42: case 46:
            case 47: case 51: case 52:
            {
                //round up
                temp += 0.5;
            }
            default:
            {
                //round down
                temp -= 0.5;
            }
        }

        return (int) temp;
    }
}
