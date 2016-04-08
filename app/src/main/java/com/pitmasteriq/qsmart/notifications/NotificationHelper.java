package com.pitmasteriq.qsmart.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.pitmasteriq.qsmart.BaseActivity;
import com.pitmasteriq.qsmart.Console;
import com.pitmasteriq.qsmart.MyApplication;
import com.pitmasteriq.qsmart.NotificationBroadcastReceiver;
import com.pitmasteriq.qsmart.Preferences;
import com.pitmasteriq.qsmart.R;
import com.pitmasteriq.qsmart.exception.ExceptionHelper;

/**
 * Created by Chris on 4/6/2016.
 */
public class NotificationHelper
{
    public static final int EXCEPTION_NOTIFICATION = 0;
    public static final int SERVICE_NOTIFICATION = 1;

    public NotificationHelper(){}

    public static void sendNotificationToUser(int id, boolean shouldSound, boolean isAlarm)
    {
        Context context = MyApplication.getAppContext();
        ExceptionHelper exceptionHelper = ExceptionHelper.get();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Notification.Builder builder = new Notification.Builder(context);

        String title = "";
        String ticker = "";
        String message = "";

        boolean isActive = prefs.getBoolean(Preferences.NOTIFICATION_ACTIVE, false);

        if (id == EXCEPTION_NOTIFICATION)
        {
            if (isActive)
            {
                if (!exceptionHelper.hasActiveExceptions())
                {
                    Console.i("Clearing notification");
                    clearNotification(id);
                    return;
                }
            }

            title = "Exception Detected";
            ticker = "Exception Detected";
            message = "";

            for (String m : exceptionHelper.getExceptionMessages())
                message += m + "\n";
        }

        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pitmaster_error_white_svg));
        builder.setSmallIcon(R.drawable.pitmaster_error_white_svg);

        if (shouldSound)
        {
            Uri sound;
            if (isAlarm && !MyApplication.isActivityVisible())
            {
                sound = Uri.parse(prefs.getString(Preferences.ALARM_SOUND,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()) );
            }
            else
            {
                sound = Uri.parse(prefs.getString(Preferences.NOTIFICATION_SOUND,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()) );
            }

            builder.setVibrate(new long[]{0, 200, 100, 200});
            builder.setSound(sound);
        }

        builder.setLights(Color.parseColor(prefs.getString(Preferences.LED_COLOR, "#ff0000ff")), 100, 100);
        builder.setAutoCancel(true);

        builder.setContentTitle(title);
        builder.setTicker(ticker);
        builder.setContentInfo(message);

        Intent cancelIntent = new Intent(context, NotificationBroadcastReceiver.class);
        cancelIntent.setAction(BaseActivity.NOTIFICATION_CANCELED);
        PendingIntent cpi = PendingIntent.getBroadcast(context, 1 , cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setDeleteIntent(cpi);

        /*
        Intent resultIntent = new Intent(context, BaseActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent rpi = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(rpi);
        */

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());

        prefs.edit().putBoolean(Preferences.NOTIFICATION_ACTIVE, true).commit();
    }

    public static Notification getServiceNotification()
    {
        Context context = MyApplication.getAppContext();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pitmaster_service_logo_white_svg));
        builder.setSmallIcon(R.drawable.pitmaster_service_logo_white_svg);
        builder.setContentTitle("qSmart is running in the background");

        /*Intent resultIntent = new Intent(context, BaseActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent rpi = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(rpi);*/

        return builder.build();
    }

    public static void clearNotification(int id)
    {
        Context context = MyApplication.getAppContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(id);
        prefs.edit().putBoolean(Preferences.NOTIFICATION_ACTIVE, false).commit();
    }
}
