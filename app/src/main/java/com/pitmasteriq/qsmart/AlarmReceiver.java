package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;

public class AlarmReceiver extends Activity
{
    static boolean active = false;
    private PowerManager.WakeLock wl;
    private Handler handler = new Handler();

    private ExceptionManager em;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_receiver);

        container = (LinearLayout) findViewById(R.id.alarm_recv_exceptions_container);

        //add list of exceptions to interface
        addExceptions();

        em = ExceptionManager.get(getApplicationContext());

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

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                em.startAlarm();
            }
        }, 500);


        handler.postDelayed(stopAlarm, BluetoothService.ALARM_WAIT_TIME);
    }



    @Override
    protected void onStop()
    {
        super.onStop();
        active = false;

        if (wl.isHeld())
            wl.release();

        em.stopAlarm();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void addExceptions()
    {
        try
        {
            HashSet<DeviceExceptions.Exception> exceptions = DeviceManager.get(getApplicationContext()).device().exceptions().get();

            if(exceptions.size() > 0)
            {
                for(DeviceExceptions.Exception e : exceptions)
                {
                    TextView temp = new TextView(this);
                    temp.setTextSize(20);
                    temp.setGravity(Gravity.CENTER_HORIZONTAL);
                    temp.setText(e.name().replace("_", " "));
                    temp.setTextColor(Color.WHITE);

                    container.addView(temp);
                }
            }
        } catch (NullPointerException e)
        {
            Log.e("tag", "Cound not add exceptions due to null pointer");
        }
    }

    public void close(View v)
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
