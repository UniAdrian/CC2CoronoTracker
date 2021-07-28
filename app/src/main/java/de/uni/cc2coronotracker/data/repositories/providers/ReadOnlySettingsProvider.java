package de.uni.cc2coronotracker.data.repositories.providers;

import android.content.Context;
import android.content.SharedPreferences;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;

public class ReadOnlySettingsProvider {
    private final SharedPreferences sharedPreferences;
    private final Context context;

    private final String TRACK_EXPOSURES;
    private final String ALLOW_TRACKING;

    public ReadOnlySettingsProvider(@ApplicationContext Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;

        TRACK_EXPOSURES = context.getResources().getString(R.string.preferences_track_exposures_key);
        ALLOW_TRACKING = context.getResources().getString(R.string.preferences_allow_tracking_key);
    }

    public boolean getTrackExposures() {
        return sharedPreferences.getBoolean(TRACK_EXPOSURES, false);
    }

    public boolean getTrackingAllowed() {
        return sharedPreferences.getBoolean(ALLOW_TRACKING, false);
    }

}
