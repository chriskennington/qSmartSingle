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
    public static final int NOTIFICATION_WAIT_TIME = 60000;

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
    private boolean notificationActive = false;


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
        mp.start();

        alarmSounding = true;

        //set the value in preferences to true
        context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.ALARM_SOUNDING, true).commit();
        return true;
    }

    public boolean stopAlarm()
    {
        try
        {
            if( mp != null && mp.isPlaying() )
            {
                mp.stop();
                mp.release();
                //set the value in preferences to true
                context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.ALARM_SOUNDING, false)
                        .putLong(Preferences.ALARM_NEXT_TIME,System.currentTimeMillis() + BluetoothService1.ALARM_WAIT_TIME).commit();

                alarmSounding = false;
                return true;
            }
        } catch(Exception e){}

        return false;
    }

    public boolean isAlarmSounding() { return alarmSounding; }
    public boolean isNotificationActive() { return notificationActive; }

    public boolean sendExceptionNotification()
    {
        if(manager == null)
        {
            Console.d("Exception Manager: notification canceled --- manager null");
            return false;
        }

        if(!canSendNotify())
        {
            Console.d("Exception Manager: notification canceled --- cant send notify");
            return false;
        }

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

        Intent resultIntent = new Intent(context, BaseActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent rpi = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(rpi);

        manager.notify(ALARM, builder.build());

        notificationActive = true;

        //set the value in preferences to true
        context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.NOTIFY_SOUNDING, true).commit();

        return true;
    }

    public Notification getServiceNotification()
    {
        //Notification notification = new Notification(R.drawable.logo, "Custom Notification", System.currentTimeMillis());

        Notification.Builder builder = new Notification.Builder(context);
/*
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_service);
        contentView.setImageViewResource(R.id.image, R.drawable.logo);
        contentView.setTextViewText(R.id.title, "qSmart is running");
        contentView.setTextViewText(R.id.text, "");
        contentView.setTextViewText(R.id.temp, "255");
        notification.contentView = contentView;
*/
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo));
        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle("qSmart Running");

        Intent resultIntent = new Intent(context, BaseActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent rpi = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(rpi);

        return builder.build();
        //return notification;
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

    public boolean canStartAlarm()
    {
        if(manager == null)
            return false;

        SharedPreferences p = context.getSharedPreferences(Preferences.PREFERENCES, 0);

        if(p.getBoolean(Preferences.ALARM_SOUNDING, false) ||
                System.currentTimeMillis() < p.getLong(Preferences.ALARM_NEXT_TIME, 0))
            return false;
        else
            return true;
    }

    public boolean cancelNotification(int id)
    {
        if (manager == null)
            return false;

        manager.cancel(id);

        notificationActive = false;


        Console.d("Exception Manager: SETTING NEW NOTIFICATION TIME!!!");

        //set the value in preferences to true
        context.getSharedPreferences(Preferences.PREFERENCES, 0).edit().putBoolean(Preferences.NOTIFY_SOUNDING, false)
                .putLong(Preferences.NOTIFY_NEXT_TIME, System.currentTimeMillis() + NOTIFICATION_WAIT_TIME).commit();
        return true;
    }
}
