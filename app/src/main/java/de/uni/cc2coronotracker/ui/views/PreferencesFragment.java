package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;

@AndroidEntryPoint
public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_screen, rootKey);
    }
}