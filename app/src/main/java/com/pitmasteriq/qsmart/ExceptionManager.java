package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by Chris on 7/31/2015.
 */
public class ExceptionManager
{
    private static final String TAG = "Alert Manager";
    public static final int INFO = 0;
    public static final int ERROR = 1;
    public static final int ALARM = 2;
    public static final int SERVICE = 3;

    private static ExceptionManager instance = null;

    private Context context;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private NotificationManager manager;
    private MediaPlayer mp = null;

    private boolean alarmSounding = false;


    public static ExceptionManager get(Context context)
    {
        if(instance == null)
            instance = new ExceptionManager(context);

        return instance;
    }

    private ExceptionManager(Context context)
    {
        if(context instanceof Activity)
            this.context = context.getApplicationContext();
        else
            this.context = context;


        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public boolean startAlarm()
    {
        if(!canStartAlarm())
            return false;

        Uri sound = Uri.parse( prefs.getString(Preferences.ALARM_SOUND,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()) );

        mp = MediaPlayer.create(context, sound);

        if (mp == null)
            return false;

        mp.start();

        alarmSounding = true;

        //set the value in preferences to true
        context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.ALARM_SOUNDING, true).commit();

        return true;
    }

    public boolean stopAlarm()
    {
        if (mp == null)
            return false;

        if( mp.isPlaying() )
        {
            mp.stop();
            //set the value in preferences to true
            context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.ALARM_SOUNDING, false).commit();

            alarmSounding = false;
            return true;
        }

        return false;
    }

    public boolean isAlarmSounding() { return alarmSounding; }

    public boolean sendExceptionNotification()
    {
        if(manager == null)
            return false;

        if(!canSendNotify())
            return false;

        Uri sound = Uri.parse( prefs.getString(Preferences.NOTIFICATION_SOUND,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()) );


        Notification.Builder builder = new Notification.Builder(context);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo));
        builder.setSmallIcon(R.drawable.logo);
        builder.setVibrate(new long[]{0, 200, 100, 200});
        builder.setSound(sound);
        builder.setLights(Color.parseColor(prefs.getString(Preferences.LED_COLOR, "#ff0000ff")), 100, 100);
        builder.setAutoCancel(true);

        builder.setContentTitle("Exception detected");
        builder.setTicker("Exception detected");
        builder.setContentInfo("One or more expections have been detected with your device.");

        Intent cancelIntent = new Intent(context, NotificationBroadcastReceiver.class);
        cancelIntent.setAction(BaseActivity.NOTIFICATION_CANCELED);
        PendingIntent cpi = PendingIntent.getBroadcast(context, 1 , cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setDeleteIntent(cpi);

        Intent resultIntent = new Intent(context, StandardMonitorActivity.class);
        resultIntent.setAction(BaseActivity.NOTIFICATION_ACK);
        PendingIntent rpi = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //builder.setContentIntent(rpi);

        manager.notify(ALARM, builder.build());


        //set the value in preferences to true
        context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.NOTIFY_SOUNDING, true).commit();

        return true;
    }

    private boolean canSendNotify()
    {
        if(manager == null)
            return false;

        SharedPreferences p = context.getSharedPreferences(Preferences.PREFERENCES, 0);

        if(p.getBoolean(Preferences.NOTIFY_SOUNDING, false) ||
            System.currentTimeMillis() < p.getLong(Preferences.NOTIFY_NEXT_TIME, 0))
                return false;
        else
            return true;

    }

    private boolean canStartAlarm()
    {
        if(manager == null)
            return false;

        if(context.getSharedPreferences(Preferences.PREFERENCES, 0).getBoolean(Preferences.ALARM_SOUNDING, false))
            return false;
        else
            return true;
    }

    public boolean cancelNotification(int id)
    {
        if (manager == null)
            return false;

        manager.cancel(id);

        //set the value in preferences to true
        context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.NOTIFY_SOUNDING, false)
                .putLong(Preferences.NOTIFY_NEXT_TIME, System.currentTimeMillis() + BaseActivity.NOTIFICATION_WAIT_TIME).commit();
        return true;
    }
}
