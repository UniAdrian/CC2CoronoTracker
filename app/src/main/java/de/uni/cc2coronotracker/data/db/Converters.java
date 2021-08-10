package de.uni.cc2coronotracker.data.db;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;
import java.util.UUID;

public class Converters {

    @TypeConverter
    public static String fromUUID(UUID uuid) {
        if (uuid == null)
            return null;
        return uuid.toString();
    }

    @TypeConverter
    public static UUID uuidFromString(String string) {
        if (string == null)
            return null;
        return UUID.fromString(string);
    }

    @TypeConverter
    public static String stringFromUri(Uri uri) {
        if (uri == null)
            return null;
        return uri.toString();
    }

    @TypeConverter
    public static Uri uriFromString(String string) {
        if (string == null)
            return null;
        return Uri.parse(string);
    }

    @TypeConverter
    public static long timestampFromDate(Date date) {
        return date.getTime();
    }

    @TypeConverter
    public static Date dateFromTimestamp(long timestamp) {
        return new Date(timestamp);
    }


    @TypeConverter
    public static String stringFromLatLang(LatLng latLang) {
        if (latLang == null) return null;
        return latLang.latitude + ";" + latLang.longitude;
    }

    @TypeConverter
    public static LatLng latLangFromString(String latLang) {
        if (latLang == null) return null;

        String[] components = latLang.split(";");
        double latitude = Double.valueOf(components[0]);
        double longitude = Double.valueOf(components[1]);
        return new LatLng(latitude, longitude);
    }

}
