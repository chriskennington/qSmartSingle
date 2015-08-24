package com.pitmasteriq.qsmartsingle;

/**
 * Created by Chris on 7/24/2015.
 */
public class Device
{
    public static final int DEVICE_NAME = 100;
    public static final int FOOD_1_PROBE_NAME = 200;
    public static final int FOOD_2_PROBE_NAME = 300;

    private DeviceConfig config = new DeviceConfig();
    private DeviceExceptions exceptions = new DeviceExceptions();
    private Probe pitProbe = new Probe("Pit Temp");
    private Probe food1Probe = new Probe("Food 1");
    private Probe food2Probe = new Probe("Food 2");





    private String macAddress = null;
    private String name = null;
    private String definedName = null;

    private long lastKnownConnectionTime = 0L;
    private long lastUpdateTime = 0L;

    private int status = Status.Unknown;

    private boolean isActive = false;
    private boolean hasConnection = false;
    private boolean hasAlarm = false;
    private boolean hasNotification = false;


    private int blowerPower = 0;







    public Device(String macAddress)
    {
        this.setAddress(macAddress);

        String n = macAddress.replace(":", "");
        String s = "pmIQ-IQ130-" + n.substring(n.length()-4);
        this.setName(s);
        this.setDefinedName(s);
    }






    public DeviceConfig config(){return config;}
    public void setConfig(DeviceConfig config){this.config = config;}

    public DeviceExceptions exceptions(){return exceptions;}

    public Probe pitProbe() {return pitProbe;}
    public void setPitProbe(Probe pitProbe) {this.pitProbe = pitProbe;}

    public Probe food1Probe() {return food1Probe;}
    public void setFood1Probe(Probe food1Probe) {this.food1Probe = food1Probe;}

    public Probe food2Probe() {return food2Probe;}
    public void setFood2Probe(Probe food2Probe) {this.food2Probe = food2Probe;}

    public int getBlowerPower() {return blowerPower;}
    public void setBlowerPower(int blowerPower) {this.blowerPower = blowerPower;}

    public String getAddress() {return macAddress;}
    public void setAddress(String macAddress) {this.macAddress = macAddress;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public boolean hasConnection() {return hasConnection;}
    public void setHasConnection(boolean hasConnection) {this.hasConnection = hasConnection;}

    public boolean hasAlarm() {return hasAlarm;}
    public void setHasAlarm(boolean hasAlarm) {this.hasAlarm = hasAlarm;}

    public boolean hasNotification() {return hasNotification;}
    public void setHasNotification(boolean hasNotification) {this.hasNotification = hasNotification;}

    public String getDefinedName() {return definedName;}
    public void setDefinedName(String definedName) {this.definedName = definedName;}

    public long getLastKnownConnectionTime() {return lastKnownConnectionTime;}
    public void setLastKnownConnectionTime(long lastKnownConnectionTime) {this.lastKnownConnectionTime = lastKnownConnectionTime;}

    public long getLastUpdateTime() {return lastUpdateTime;}
    public void setLastUpdateTime(long update) {this.lastUpdateTime = update;}

    public int getStatus(){return status;}
    public void setStatus(int status){this.status = status;}

    public boolean isActive(){return isActive;}
    public void setActive(boolean isActive){this.isActive = isActive;}























    public class Probe
    {
        private String defaultName = null;
        private String name = null;
        private int temperature = 0;

        public Probe(String defaultName){this.defaultName = defaultName;}

        public String getName()
        {
            if( name == null )
                return defaultName;

            return name;
        }

        public void setName(String name) {this.name = name;}

        public int getTemperature() {return temperature;}
        public void setTemperature(int temperature) {this.temperature = temperature;}
    }










    public class Status
    {
        public static final int Unknown = -1;
        public static final int OK = 0;
        public static final int Disconnected = 1;
        public static final int NoData = 2; 		//has not received data in X seconds
        public static final int LostConnection = 3;
    }
}
