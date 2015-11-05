package com.pitmasteriq.qsmart;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pitmasteriq.qsmart.export.ExportDataActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setupActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    private void setupActionBar()
    {
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        private SharedPreferences prefs;
        private SharedPreferences.Editor editor;

        private RingtonePreference notifSound;
        private RingtonePreference alarmSound;
        private EditTextPreference passcode;


        public SettingsFragment()
        {
            // Required empty public constructor
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            editor = prefs.edit();

            notifSound = (RingtonePreference)findPreference(Preferences.NOTIFICATION_SOUND);
            alarmSound = (RingtonePreference)findPreference(Preferences.ALARM_SOUND);
            passcode = (EditTextPreference)findPreference(Preferences.PASSCODE);

            passcode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if( newValue.toString().length() < 4)
                    {
                        MessageDialog md = MessageDialog.newInstance("Error", "Passcode must be a 4 digit number");
                        md.show(getFragmentManager(), "dialog");
                        return false;
                    }

                    return true;
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.fragment_preferences, container, false);
            return v;
        }



        @Override
        public void onResume()
        {
            super.onResume();

            notifSound.setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
            alarmSound.setRingtoneType(RingtoneManager.TYPE_ALARM);

            String code = prefs.getString(Preferences.PASSCODE, "0000");

            while (code.length() < 4)
                code = "0" + code;


            passcode.setText(code);
        }
    }

    public void onExportDataClick(View v)
    {
        Log.e("TAG", "exporting data");
        startActivity(new Intent(this, ExportDataActivity.class));
    }
}
