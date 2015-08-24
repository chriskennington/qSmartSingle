package com.pitmasteriq.qsmartsingle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerState;

//TODO 's


public class MainActivity extends Activity
{

    private BleManager bleManager;


    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bleManager = BleManager.get(getApplicationContext());


        prefs = getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE);
        editor = prefs.edit();


        //check version information
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;;

            int currentVersion = prefs.getInt(Preferences.VERSION_CODE, -1);
            if( currentVersion == -1 || currentVersion != versionCode )
            {
                editor.putBoolean(Preferences.NEW_VERSION, true);
                editor.putInt(Preferences.VERSION_CODE, versionCode);
                editor.apply();
            }

        } catch(PackageManager.NameNotFoundException e){e.printStackTrace();}



        //TODO start up stuff
        Handler handler = new Handler();

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                initializeBluetooth();
            }
        }, 500);

    }

    private void initializeBluetooth()
    {
        if(bleManager.is(BleManagerState.OFF))
        {
            //attempt to start bluetooth with user permission
            bleManager.turnOnWithIntent(this, 100);
        }
        else
        {
            //TODO start service
            if(startService(new Intent(this, BluetoothService.class)) != null)
            {
                startActivity(new Intent(this, StandardMonitorActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 100)
        {
            if( resultCode == Activity.RESULT_OK)
            {
                //TODO start service
                if(startService(new Intent(this, BluetoothService.class)) != null)
                {
                    startActivity(new Intent(this, StandardMonitorActivity.class));
                    finish();
                }
            }
            else
            {
                //Bluetooth was not turned on
                //TODO add message stating bt is required
                Toast.makeText(this, "Bluetooth is required.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
