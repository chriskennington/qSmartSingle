package com.pitmasteriq.qsmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver
{
	private static final String TAG = "Notification Receiver";
	
	
	public NotificationBroadcastReceiver(){}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();

		Console.e("*** Notification canceled ***");

		ExceptionManager.get(context.getApplicationContext()).cancelNotification(ExceptionManager.ALARM);

	}
}
