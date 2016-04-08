package com.pitmasteriq.qsmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pitmasteriq.qsmart.notifications.NotificationHelper;

public class NotificationBroadcastReceiver extends BroadcastReceiver
{
	private static final String TAG = "Notification Receiver";
	
	
	public NotificationBroadcastReceiver(){}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();

		Console.e("*** Notification canceled ***");
		NotificationHelper.clearNotification(NotificationHelper.EXCEPTION_NOTIFICATION);
	}
}
