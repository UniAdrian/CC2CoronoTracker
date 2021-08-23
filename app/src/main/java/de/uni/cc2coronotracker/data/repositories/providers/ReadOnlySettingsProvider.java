package de.uni.cc2coronotracker.data.repositories.providers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;

/**
 * Injected into e.g. Viewmodels provides read-only access to the users preferences.
 * The user can change those via the dedicated Prefrences screen.
 */
public class ReadOnlySettingsProvider {
    private final SharedPreferences sharedPreferences;
    private final Context context;

    private final String TRACK_EXPOSURES;
    private final String ALLOW_TRACKING;
    private final String UUID_KEY;

    public ReadOnlySettingsProvider(@ApplicationContext Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;

        TRACK_EXPOSURES = context.getResources().getString(R.string.preferences_track_exposures_key);
        ALLOW_TRACKING = context.getResources().getString(R.string.preferences_allow_tracking_key);
        UUID_KEY = context.getResources().getString(R.string.preferences_personal_uuid_key);
    }

    /**
     * If true and the user allows general tracking of location data, the app tracks exposures locations.
     * @return True if enabled, false otherwise+
     * @apiNote If getTrackExposures() is false, this always returns false as well.
     */
    public boolean getTrackExposures() {
        boolean tracking_allowed = sharedPreferences.getBoolean(ALLOW_TRACKING, false);
        return tracking_allowed && sharedPreferences.getBoolean(TRACK_EXPOSURES, false);
    }

    /**
     * If true the app allows other apps to track the exposures (sets the corresponding flag in the QR code)
     * @return True if the user wants to be tracked location wise, false otherwise.
     * @apiNote If false, getTrackExposures() always returns false as well.
     */
    public boolean getTrackingAllowed() {
        return sharedPreferences.getBoolean(ALLOW_TRACKING, false);
    }

    /**
     * Returns the personal UUID/AppUUID if possible.
     * @return The UUID
     * @throws IllegalArgumentException If the string stored as {@code UUID_KEY} is not a valid UUID
     * @throws IllegalStateException If no uuid string is stored under {@code UUID_KEY} yet
     */
    public UUID getPersonalUUID() {
        String asString = sharedPreferences.getString(UUID_KEY, null);
        if (asString == null)
            throw new IllegalStateException();
        return UUID.fromString(asString);
    }
}
