package de.uni.cc2coronotracker.data.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

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
public class Contact implements Parcelable {

    // Required default constructor
    public Contact() {
    }

    @PrimaryKey(autoGenerate = true)
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


    @NonNull
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



    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public Contact(Parcel in){
        this.id = in.readLong();
        this.uuid = (UUID) in.readSerializable();
        this.displayName = in.readString();
        // Boolean support requires a higher API level...
        this.favorite = in.readInt() == 1;
        this.photoUri = in.readParcelable(Contact.class.getClassLoader());
        this.lookupUri = in.readParcelable(Contact.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeSerializable(uuid);
        parcel.writeString(displayName);
        // Boolean support requires a higher API level...
        parcel.writeInt((favorite)?1:0);
        parcel.writeParcelable(photoUri, flags);
        parcel.writeParcelable(lookupUri, flags);
    }
}
