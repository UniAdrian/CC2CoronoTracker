package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import de.uni.cc2coronotracker.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_screen, rootKey);
    }
}