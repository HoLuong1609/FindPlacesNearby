package com.motthoidecode.findplacesnearby;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import utils.Util;
import views.NumberPickerPreferences;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.app_preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NumberPickerPreferences numberPickerPreferences = (NumberPickerPreferences)findPreference(Util.KEY_RADIUS_PICKER);
        numberPickerPreferences.setSummary(numberPickerPreferences.getSharedPreferences().getInt(Util.KEY_RADIUS_PICKER,0) + " Km");
    }
}