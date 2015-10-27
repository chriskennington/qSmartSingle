package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by Chris on 9/10/2015.
 */
public class Notifications
{
    public static Notification getServiceNotification(Context context)
    {
        Context c = getApplicationContext(context);

        //Notification notification = new Notification(R.drawable.logo, "Custom Notification", System.currentTimeMillis());

        Notification.Builder builder = new Notification.Builder(c);


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

        Intent resultIntent = new Intent(c, BaseActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
        // Adds the back stack
        stackBuilder.addParentStack(BaseActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        resultIntent.setAction(BaseActivity.NOTIFICATION_ACK);
        PendingIntent rpi = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(rpi);

        return builder.build();
        //return notification;
    }

    public static Notification getExceptionNotification(Context context)
    {
        Context c = getApplicationContext(context);

        Notification.Builder builder = new Notification.Builder(c);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Uri sound = Uri.parse( prefs.getString(Preferences.NOTIFICATION_SOUND,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()) );

        builder.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.logo));
        builder.setSmallIcon(R.drawable.logo);
        builder.setVibrate(new long[]{0, 200, 100, 200});
        builder.setSound(sound);
        builder.setLights(Color.parseColor(prefs.getString(Preferences.LED_COLOR, "#ff0000ff")), 500, 500);
        builder.setAutoCancel(true);

        builder.setContentTitle("Exception detected");
        builder.setTicker("Exception detected");
        builder.setContentInfo("One or more expections have been detected with your device.");

        Intent cancelIntent = new Intent(c, NotificationBroadcastReceiver.class);
        cancelIntent.setAction(BaseActivity.NOTIFICATION_CANCELED);
        PendingIntent cpi = PendingIntent.getBroadcast(c, 1 , cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setDeleteIntent(cpi);

        Intent resultIntent = new Intent(c, BaseActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
        // Adds the back stack
        stackBuilder.addParentStack(BaseActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        resultIntent.setAction(BaseActivity.NOTIFICATION_ACK);
        PendingIntent rpi = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent rpi = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(rpi);

        return builder.build();
    }


    private static Context getApplicationContext(Context c)
    {
        if (c instanceof Activity)
            return c.getApplicationContext();
        else
            return c;
    }
}
