package com.climbingweather.cw;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        addPreferencesFromResource(R.layout.preferences);
        ListPreference lp = (ListPreference) findPreference("tempUnit");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Logger.log(prefs.getString("tempUnit", "f"));
        lp.setSummary(prefs.getString("tempUnit", "f").equals("f") ? "Fahrenheit" : "Celsius");
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        startActivity(new Intent(PreferencesActivity.this, MainActivity.class)); 
        return true;
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }
}
