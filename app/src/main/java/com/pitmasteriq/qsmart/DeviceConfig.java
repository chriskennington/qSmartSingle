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

    private int pitSet = 0;
    private int food1AlarmTemp = 0;
    private int food2AlarmTemp = 0;
    private int pitAlarmDeviation = 0;
    private int lidDetect = 0;
    private int fanSpeed = 0;
    private int tempUnits = 0;
    private int delayTime = 0;
    private int delayPitSet = 0;
    private int food1Temp = 0;
    private int food1PitSet = 0;
    private int food2Temp = 0;
    private int food2PitSet = 0;
    private int minutesPast = 0;


    public String getHash() {return hash;}
    public void setHash(String hash) {this.hash = hash;}

    public int getPitSet() {return pitSet;}
    public void setPitSet(int pitSet) {this.pitSet = pitSet;}

    public int getFood1AlarmTemp() {return food1AlarmTemp;}
    public void setFood1AlarmTemp(int food1AlarmTemp) {this.food1AlarmTemp = food1AlarmTemp;}

    public int getFood2AlarmTemp() {return food2AlarmTemp;}
    public void setFood2AlarmTemp(int food2AlarmTemp) {this.food2AlarmTemp = food2AlarmTemp;}

    public int getPitAlarmDeviation() {return pitAlarmDeviation;}
    public void setPitAlarmDeviation(int pitAlarmDeviation) {this.pitAlarmDeviation = pitAlarmDeviation;}

    public int getLidDetect() {return lidDetect;}
    public void setLidDetect(int lidDetect) {this.lidDetect = lidDetect;}

    public int getFanSpeed() {return fanSpeed;}
    public void setFanSpeed(int fanSpeed) {this.fanSpeed = fanSpeed;}

    public int getTempUnits() {return tempUnits;}
    public void setTempUnits(int tempUnits) {this.tempUnits = tempUnits;}

    public int getDelayTime() {return delayTime;}
    public void setDelayTime(int delayTime) {this.delayTime = delayTime;}

    public int getDelayPitSet() {return delayPitSet;}
    public void setDelayPitSet(int delayPitSet) {this.delayPitSet = delayPitSet;}

    public int getFood1Temp() {return food1Temp;}
    public void setFood1Temp(int food1Temp) {this.food1Temp = food1Temp;}

    public int getFood1PitSet() {return food1PitSet;}
    public void setFood1PitSet(int food1PitSet) {this.food1PitSet = food1PitSet;}

    public int getFood2Temp() {return food2Temp;}
    public void setFood2Temp(int food2Temp) {this.food2Temp = food2Temp;}

    public int getFood2PitSet() {return food2PitSet;}
    public void setFood2PitSet(int food2PitSet) {this.food2PitSet = food2PitSet;}

    public int getMinutesPast() { return minutesPast; }
    public void setMinutesPast(int minutesPast) { this.minutesPast = minutesPast; }
}
