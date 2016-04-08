package com.pitmasteriq.qsmart.exception;

import com.pitmasteriq.qsmart.notifications.NotificationHelper;

import java.util.ArrayList;

/**
 * Created by Chris on 3/29/2016.
 */
public class ExceptionHelper
{
    private static ExceptionHelper instance = null;
    private ArrayList<Exception> exceptions = new ArrayList<>();

    public static ExceptionHelper get()
    {
        if (instance == null)
            instance = new ExceptionHelper();

        return instance;
    }

    private ExceptionHelper()
    {
        initExceptions();
    }

    private void initExceptions()
    {
        exceptions.clear();
        exceptions.add(new Exception(0, Exception.ENCLOSURE_HOT, 1, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(1, Exception.FOOD_PROBE_1_ERROR, 2, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(2, Exception.FOOD_PROBE_2_ERROR, 4, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(3, Exception.FOOD_1_DONE, 8, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(4, Exception.FOOD_2_DONE, 16, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(5, Exception.PIT_HOT,32, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(6, Exception.PIT_COLD, 64, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(7, Exception.LID_OFF, 128, Exception.ExceptionType.NOTIFY));
        exceptions.add(new Exception(8, Exception.DELAY_PIT_SET, 256, Exception.ExceptionType.GENERAL));
        exceptions.add(new Exception(9, Exception.FOOD_1_TEMP_PIT_SET, 512, Exception.ExceptionType.NOTIFY));
        exceptions.add(new Exception(10, Exception.FOOD_2_TEMP_PIT_SET, 1024, Exception.ExceptionType.NOTIFY));
        exceptions.add(new Exception(11, Exception.PIT_PROBE_ERROR, 2048, Exception.ExceptionType.ALARM));
        exceptions.add(new Exception(12, Exception.FOOD_PROBE_1_NOT_PRESENT, 4096, Exception.ExceptionType.GENERAL));
        exceptions.add(new Exception(13, Exception.FOOD_PROBE_2_NOT_PRESENT, 8192, Exception.ExceptionType.GENERAL));
        exceptions.add(new Exception(14, Exception.CONNECTION_LOST, 16384, Exception.ExceptionType.ALARM, 500));
    }

    public void activateException(int id)
    {
        exceptions.get(id).setActive(true);
        if (exceptions.get(id).getType() == Exception.ExceptionType.ALARM)
            NotificationHelper.sendNotificationToUser(NotificationHelper.EXCEPTION_NOTIFICATION, true, true);
        else if (exceptions.get(id).getType() == Exception.ExceptionType.NOTIFY)
            NotificationHelper.sendNotificationToUser(NotificationHelper.EXCEPTION_NOTIFICATION, true, false);
    }
    public void deactivateException(int id)
    {
        exceptions.get(id).setActive(false); unsilenceException(id);
        NotificationHelper.sendNotificationToUser(NotificationHelper.EXCEPTION_NOTIFICATION, false, false);
    }
    public void silenceException(int id)
    {
        exceptions.get(id).setSilenced(true);
        NotificationHelper.sendNotificationToUser(NotificationHelper.EXCEPTION_NOTIFICATION, false, false);
    }
    public void unsilenceException(int id)
    {
        exceptions.get(id).setSilenced(false);
    }

    public boolean hasActiveExceptions()
    {
        for (Exception e : exceptions)
            if (e.isActive() && !e.isSilenced())
                return true;

        return false;
    }

    public boolean hasSilencedExceptions()
    {
        for (Exception e : exceptions)
            if (e.isActive() && e.isSilenced())
                return true;

        return false;
    }

    public boolean isExceptionActive(int id)
    {
        return exceptions.get(id).isActive();
    }

    /**
     * @return a list of active exceptions
     */
    public ArrayList<Exception> getActiveExceptions()
    {
        ArrayList<Exception> active = new ArrayList<>();

        for (Exception e : exceptions)
        {
            if (e.isActive() && !e.isSilenced())
                active.add(e);
        }

        return active;
    }

    /**
     * @return a list of silenced exceptions
     */
    public ArrayList<Exception> getSilencedExceptions()
    {
        ArrayList<Exception> silenced = new ArrayList<>();

        for (Exception e : exceptions)
        {
            if (e.isActive() && e.isSilenced())
                silenced.add(e);
        }

        return silenced;
    }


    public String getExceptionMessage(int id)
    {
        return exceptions.get(id).getName();
    }

    /**
     * @return a list of strings for all active and unsilenced exceptions
     */
    public ArrayList<String> getExceptionMessages()
    {
        ArrayList<String> messages = new ArrayList<>();
        for (Exception e : exceptions)
            if (e.isActive() && !e.isSilenced())
                messages.add(e.getName());

        return messages;
    }
}
