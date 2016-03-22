package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

/**
 * Created by Chris on 7/24/2015.
 */
public class DeviceManager
{
    private static DeviceManager instance = null;
    private Context context;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public static DeviceManager get(Context context)
    {
        if(instance == null)
            instance = new DeviceManager(context);

        return instance;
    }

    private DeviceManager(Context context)
    {
        if(context instanceof Activity)
            this.context = context.getApplicationContext();
        else
            this.context = context;
    }

    //********************************************************************

    private static final String SAVED_ADDRESS_PREFIX = "com.pitmateriq.qsmart.";
    private Device device = null;

    public void clear()
    {
        device = null;
    }

    public void newDevice(String address)
    {
        if(device != null)
            saveDevice();

        device = new Device(address);
        loadDevice();
    }

    /**
     * Get the currently held device object
     * @return
     */
    public Device device() throws NullDeviceException
    {
        if (device == null)
            throw new NullDeviceException("Device is null");
        else
            return device;
    }

    public boolean hasDevice()
    {
        return (device != null);
    }


    /**
     * Attempts to load a device from memory. If a file does not exist for a device, do nothing
     */
    private void loadDevice()
    {
        long start = System.currentTimeMillis();
        Console.i("load: loading device");

        if(!hasSavedFile())
        {
            Console.i("load: no saved file");
            return;
        }

        device.setDefinedName(prefs.getString("name", null));
        device.pitProbe().temperature().set(prefs.getInt("pit_temp", -1));

        device.food1Probe().temperature().set(prefs.getInt("food_1_temp", -1));
        device.food1Probe().setName(prefs.getString("food_1_probe_name", null));

        device.food2Probe().temperature().set(prefs.getInt("food_2_temp", -1));
        device.food2Probe().setName(prefs.getString("food_2_probe_name", null));

        device.setLastUpdateTime(prefs.getLong("last_update_time", -1l));
        device.setLastKnownConnectionTime(prefs.getLong("last_connection_time", -1l));

        device.setStatus(prefs.getInt("status", Device.Status.Unknown));
        device.setHasConnection(prefs.getBoolean("has_connection", false));
        device.setHasAlarm(prefs.getBoolean("has_alarm", false));
        device.setHasNotification(prefs.getBoolean("has_notification", false));
        device.setBlowerPower(prefs.getInt("blower_power", -1));

        device.exceptions().loadExceptions(prefs.getInt("exceptions", 0));
        device.exceptions().loadSilenced(prefs.getInt("silenced", 0));

        Console.i("load: finished loading device - " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * Saves a device to file for future use
     */
    public void saveDevice()
    {
        long start = System.currentTimeMillis();

        Console.i("save: saving device");

        if(device == null)
        {
            Console.i("save: save failed");
            return;
        }


        prefs = context.getSharedPreferences(SAVED_ADDRESS_PREFIX + device.getAddress(), Context.MODE_PRIVATE);
        editor = prefs.edit();

        editor.putString("address", device.getAddress());
        editor.putString("name", device.getDefinedName());
        editor.putInt("pit_temp", device.pitProbe().temperature().getRawTemp());

        editor.putInt("food_1_temp", device.food1Probe().temperature().getRawTemp());
        editor.putString("food_1_probe_name", device.food1Probe().getName());

        editor.putInt("food_2_temp", device.food2Probe().temperature().getRawTemp());
        editor.putString("food_2_probe_name", device.food2Probe().getName());

        editor.putLong("last_update_time", device.getLastUpdateTime());
        editor.putLong("last_connection_time", device.getLastKnownConnectionTime());

        editor.putInt("status", device.getStatus());
        editor.putBoolean("has_connection", device.hasConnection());
        editor.putBoolean("has_alarm", device.hasAlarm());
        editor.putBoolean("has_notification", device.hasNotification());
        editor.putInt("blower_power", device.getBlowerPower());

        editor.putInt("exceptions", device.exceptions().getExceptionsValue());
        editor.putInt("silenced", device.exceptions().getSilencedValue());

        editor.commit();

        Console.i("save: finished saving device - " + (System.currentTimeMillis() - start) + "ms");
    }


    private boolean hasSavedFile()
    {
        prefs = context.getSharedPreferences(SAVED_ADDRESS_PREFIX + device.getAddress(), Context.MODE_PRIVATE);
        return prefs.contains("address");
    }


    public boolean updateValues(List<Short> values)
    {
        try
        {
            device.config().pitAlarmDeviation().set(values.get(0));
            device.config().setDelayTime(values.get(1));
            device.config().delayPitSet().set(values.get(2));
            device.pitProbe().temperature().set(values.get(3));
            device.food1Probe().temperature().set(values.get(4));
            device.food2Probe().temperature().set(values.get(5));
            device.config().pitSet().set(values.get(6));
            device.config().food1AlarmTemp().set(values.get(7));
            device.config().food2AlarmTemp().set(values.get(8));
            device.setBlowerPower(values.get(9));
            device.config().food1Temp().set(values.get(10));
            device.config().food1PitSet().set(values.get(11));
            device.config().food2Temp().set(values.get(12));
            device.config().food2PitSet().set(values.get(13));

            //TODO add minutes past somehow??
            device.config().setMinutesPast(values.get(14
            ));
        }
        catch(NullPointerException e)
        {
            return false;
        }

        return true;
    }
}
