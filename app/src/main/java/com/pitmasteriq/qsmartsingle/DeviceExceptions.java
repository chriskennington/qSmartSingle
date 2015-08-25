package com.pitmasteriq.qsmartsingle;

import java.util.HashSet;

/**
 * Created by Chris on 7/30/2015.
 */
public class DeviceExceptions
{
    enum ExceptionType
    {
        ALARM, NOTIFY, GENERAL;
    }

    enum Exception
    {
        ENCLOSURE_HOT               (0, 1, ExceptionType.ALARM),
        PIT_PROBE_ERROR             (11, 2048, ExceptionType.ALARM),
        FOOD_1_PROBE_ERROR          (1, 2, ExceptionType.ALARM),
        FOOD_2_PROBE_ERROR          (2, 4, ExceptionType.ALARM),
        FOOD_1_DONE                 (3, 8, ExceptionType.ALARM),
        FOOD_2_DONE                 (4, 16, ExceptionType.ALARM),
        PIT_HOT                     (5, 32, ExceptionType.ALARM),
        PIT_COLD                    (6, 64, ExceptionType.ALARM),
        LID_OFF                     (7, 128, ExceptionType.NOTIFY),
        DELAY_PIT_SET               (8, 256, ExceptionType.NOTIFY),
        FOOD_1_TEMP_PIT_SET         (9, 512, ExceptionType.NOTIFY),
        FOOD_2_TEMP_PIT_SET         (10, 1024, ExceptionType.NOTIFY),
        FOOD_1_PROBE_NOT_PRESENT    (12, 4096, ExceptionType.GENERAL),
        FOOD_2_PROBE_NOT_PRESENT    (13, 8192, ExceptionType.GENERAL),
        CONNECTION_LOST             (14, 16384, ExceptionType.ALARM);

        private final int number;
        private final int value;
        private final ExceptionType type;

        Exception(int number, int value, ExceptionType type)
        {
            this.number = number;
            this.value = value;
            this.type = type;
        }
    }

    private HashSet<Exception> exceptions;
    private HashSet<Exception> silenced;

    private String hash = null;

    public DeviceExceptions()
    {
        exceptions = new HashSet<>();
        silenced = new HashSet<>();
    }

    /**
     * Checks if any exceptions
     * @return
     */
    public boolean hasException()
    {
        if(exceptions.size() > 0)
            return true;
        else
            return false;
    }

    public boolean hasAlarm()
    {
        if(exceptions.size() == 0)
            return false;

        for(Exception e : exceptions)
            if(e.type == ExceptionType.ALARM)
                return true;

        return false;
    }

    public boolean hasNotify()
    {
        if(exceptions.size() == 0)
            return false;

        for(Exception e : exceptions)
            if(e.type == ExceptionType.NOTIFY)
                return true;

        return false;
    }

    public boolean hasSilenced()
    {
        if(silenced.size() > 0)
            return true;
        else
            return false;
    }

    public HashSet<Exception> get()
    {
        return exceptions;
    }

    public HashSet<Exception> silenced()
    {
        return silenced;
    }

    public int getExceptionsValue()
    {
        int i = 0;
        for(Exception e : exceptions)
            i = i + e.value;

        return i;
    }

    public int getSilencedValue()
    {
        int i = 0;
        for(Exception e : silenced)
            i = i + e.value;

        return i;
    }

    public boolean addException(Exception e)
    {
        if(!silenced().contains(e))
            return exceptions.add(e);
        else
            return false;
    }

    public boolean addException(int i)
    {
        for(Exception e : Exception.values())
        {
            if(e.number == i)
            {
                if(!silenced().contains(e))
                    return addException(e);
                else
                    return false;
            }
        }

        return false;
    }

    public boolean removeException(Exception e)
    {
        unsilenceException(e);
        return exceptions.remove(e);
    }

    public boolean removeException(int i)
    {
        for(Exception e : Exception.values())
        {
            if(e.number == i)
            {
                unsilenceException(e);
                return removeException(e);
            }
        }
        return false;
    }

    public boolean silenceException(Exception e)
    {
        removeException(e);
        return silenced.add(e);
    }

    public void loadExceptions(int value)
    {
        if(value == 0)
            return;

        int i = value;
        int max = 16384;

        for(int x=15; x > 0; x--)
        {
            if(i >= max)
            {
                for(Exception e : Exception.values())
                    if(e.number == x-1)
                        addException(e);

                i -= max;
            }

            if (i == 0)
                break;

            max /= 2;
        }
    }

    public void loadSilenced(int value)
    {
        if(value == 0)
            return;

        int i = value;
        int max = 16384;

        for(int x=15; x > 0; x--)
        {
            if(i >= max)
            {
                for(Exception e : Exception.values())
                    if(e.number == x-1)
                        silenceException(e);

                i -= max;
            }

            if (i == 0)
                break;

            max /= 2;
        }
    }

    public boolean compareHash(String hash)
    {
        if(this.hash == null)
        {
            this.hash = hash;
            return false;
        }

        if(this.hash.equals(hash))
            return true;
        else
        {

            this.hash = hash;
            return false;
        }
    }

    private void unsilenceException(Exception e)
    {
        silenced.remove(e);
    }
}
