package de.uni.cc2coronotracker.data.models;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Models a single uuid-holder potentially linked with a phone contact.
 */
@Entity(
        tableName = "contacts",
        indices = {
            @Index(value = {"uuid"}, unique = true),
            @Index(value = {"lookup_uri"}, unique = true)
        }
)
public class Contact {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public long id;

    /**
     * If non null this contact has been permanently bound to a UUID
     */
    @ColumnInfo(name = "uuid")
    public UUID uuid;

    @ColumnInfo(name = "display_name")
    public String displayName;
    @ColumnInfo(name = "is_favorite")
    public boolean favorite;
    @ColumnInfo(name = "photo_uri")
    public Uri photoUri;

    /**
     * If non-null this contact was imported from the phone contacts.
     */
    @ColumnInfo(name = "lookup_uri")
    public Uri lookupUri;


    @Override
    public String toString() {
        return String.format("Contact [id=%d, uuid=%s, lookupUri=%s, displayName=%s, photoUri=%s, starred=%b]", id, uuid, lookupUri, displayName, photoUri, favorite);
    }

    /**
     * @return True if the contact is connected to a phone contact, false otherwise.
     */
    public boolean isBound() {
        return lookupUri != null;
    }
}
