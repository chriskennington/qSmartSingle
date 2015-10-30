package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class AlarmReceiver extends Activity
{
    static boolean active = false;
    private PowerManager.WakeLock wl;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_receiver);

        Log.e("TAG", "alarm");

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "alarmReceiver");

        wl.acquire(5000);

        final Window win= getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        active = true;

        ExceptionManager.get(getApplicationContext()).startAlarm();
        handler.postDelayed(stopAlarm, BluetoothService.ALARM_WAIT_TIME);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        active = false;

        if (wl.isHeld())
            wl.release();

        ExceptionManager.get(getApplicationContext()).stopAlarm();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public void silence(View v)
    {
        finish();
    }


    private Runnable stopAlarm = new Runnable()
    {
        @Override
        public void run()
        {
            finish();
        }
    };
}
