package com.pitmasteriq.qsmart.exception;

/**
 * Created by Chris on 3/29/2016.
 */
public class Exception
{
    public static final int NOTIFICATION_INTERVAL = 60000;
    public static final int DEFAULT_BLINK_RATE = 250;

    public static final String ENCLOSURE_HOT            = "Enclosure Hot";
    public static final String PIT_PROBE_ERROR          = "Pit Probe Error";
    public static final String FOOD_PROBE_1_ERROR       = "Food Probe 1 Error";
    public static final String FOOD_PROBE_2_ERROR       = "Food Probe 2 Error";
    public static final String FOOD_1_DONE              = "Food 1 Done";
    public static final String FOOD_2_DONE              = "Food 2 Done";
    public static final String PIT_HOT                  = "Pit Hot";
    public static final String PIT_COLD                 = "Pit Cold";
    public static final String LID_OFF                  = "Lid Off";
    public static final String DELAY_PIT_SET            = "Delay Pit Set Activated";
    public static final String FOOD_1_TEMP_PIT_SET      = "Food 1 Temp Pit Set Activated";
    public static final String FOOD_2_TEMP_PIT_SET      = "Food 2 Temp Pit Set Activated";
    public static final String FOOD_PROBE_1_NOT_PRESENT = "Food Probe 1 not Present";
    public static final String FOOD_PROBE_2_NOT_PRESENT = "Food Probe 2 not Present";
    public static final String CONNECTION_LOST          = "Connection Lost";

    enum ExceptionType
    {
        ALARM, NOTIFY, GENERAL
    }

    private boolean active = false;
    private boolean silenced = false;

    private long lastNotifyTimestamp = 0L;
    private long lastDismissedTimestamp = 0L;
    private int blinkRate = DEFAULT_BLINK_RATE;

    private ExceptionType type;
    private String name = null;
    private int id;
    private int value;

    public Exception(int id, String name, int value, ExceptionType type)
    {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Exception(int id, String name, int value, ExceptionType type, int blinkRate)
    {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
        this.blinkRate = blinkRate;
    }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }

    public void setSilenced(boolean silenced) { this.silenced = silenced; }
    public boolean isSilenced() { return silenced; }

    public boolean canNotify() { return (System.currentTimeMillis() - lastDismissedTimestamp >= NOTIFICATION_INTERVAL);}
    public void sentNotification() { lastNotifyTimestamp = System.currentTimeMillis(); }
    public void wasDismissed() { lastDismissedTimestamp = System.currentTimeMillis(); lastNotifyTimestamp = 0; }

    public int getId() { return id; }
    public int getValue() { return value; }
    public String getName() { return name; }
    public ExceptionType getType() { return type; }
    public int getBlinkRate() { return blinkRate; }
}
