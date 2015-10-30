package com.pitmasteriq.qsmart;

/**
 * Created by Chris on 7/24/2015.
 */
public class DeviceConfig
{
    private String hash = null;

    public static final int CONFIG_PIT_SET = 1;
    public static final int CONFIG_FOOD_1_ALARM = 2;
    public static final int CONFIG_FOOD_2_ALARM = 3;
    public static final int CONFIG_PIT_ALARM = 4;
    public static final int CONFIG_LID_DETECT = 5;
    public static final int CONFIG_FAN_SPEED = 6;
    public static final int CONFIG_TEMP_UNITS = 7;
    public static final int CONFIG_SOUND = 8;
    public static final int CONFIG_DISPLAY = 9;
    public static final int CONFIG_DELAY_TIME = 10;
    public static final int CONFIG_DELAY_PIT_SET = 11;
    public static final int CONFIG_FOOD_1_TEMP = 12;
    public static final int CONFIG_FOOD_1_PIT_SET = 13;
    public static final int CONFIG_FOOD_2_TEMP = 14;
    public static final int CONFIG_FOOD_2_PIT_SET = 15;

    public static final int CONFIG_DEVICE_NAME = 100;
    public static final int CONFIG_FOOD1_NAME = 101;
    public static final int CONFIG_FOOD2_NAME = 102;

    private Temperature pitSet = new Temperature();
    private Temperature food1AlarmTemp = new Temperature();
    private Temperature food2AlarmTemp = new Temperature();
    private Temperature pitAlarmDeviation = new Temperature();
    private int lidDetect = 0;
    private int fanSpeed = 0;
    private int tempUnits = 0;
    private int delayTime = 0;
    private Temperature delayPitSet = new Temperature();
    private Temperature food1Temp = new Temperature();
    private Temperature food1PitSet = new Temperature();
    private Temperature food2Temp = new Temperature();
    private Temperature food2PitSet = new Temperature();
    private int minutesPast = 0;


    public String getHash() {return hash;}
    public void setHash(String hash) {this.hash = hash;}


    public Temperature pitSet() {return pitSet;}

    public Temperature food1AlarmTemp() {return food1AlarmTemp;}

    public Temperature food2AlarmTemp() {return food2AlarmTemp;}

    public Temperature pitAlarmDeviation() {return pitAlarmDeviation;}

    public int getLidDetect() {return lidDetect;}
    public void setLidDetect(int lidDetect) {this.lidDetect = lidDetect;}

    public int getFanSpeed() {return fanSpeed;}
    public void setFanSpeed(int fanSpeed) {this.fanSpeed = fanSpeed;}

    public int getTempUnits() {return tempUnits;}
    public void setTempUnits(int tempUnits) {this.tempUnits = tempUnits;}

    public int getDelayTime() {return delayTime;}
    public void setDelayTime(int delayTime) {this.delayTime = delayTime;}

    public Temperature delayPitSet() {return delayPitSet;}

    public Temperature food1Temp() {return food1Temp;}

    public Temperature food1PitSet() {return food1PitSet;}

    public Temperature food2Temp() {return food2Temp;}

    public Temperature food2PitSet() {return food2PitSet;}

    public int getMinutesPast() { return minutesPast; }
    public void setMinutesPast(int minutesPast) { this.minutesPast = minutesPast; }
}
