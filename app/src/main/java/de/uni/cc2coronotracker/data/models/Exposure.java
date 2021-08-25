package de.uni.cc2coronotracker.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;

/**
 * Models a single exposure to another user of the app.
 */
@Entity(
    tableName= "exposures",
    foreignKeys = {
        @ForeignKey(
            entity = Contact.class,
            parentColumns = "id",
            childColumns = "contact_id",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class Exposure {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "contact_id", index = true)
    public long contactId;

    @ColumnInfo(name = "start_date")
    @NonNull
    public Date startDate;

    @ColumnInfo(name = "end_date")
    public Date endDate;

    @ColumnInfo(name = "location", defaultValue = "NULL")
    public LatLng location;

    /**
     * Required default constrcutor
     */
    public Exposure() {}

    /**
     * Copy constructor
     * @param other Exposure to copy from
     */
    public Exposure(Exposure other) {
        this.id = other.id;
        this.contactId = other.contactId;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
        this.location = other.location;
    }

    @Override
    public String toString() {
        return "Exposure{" +
                "id=" + id +
                ", contactId=" + contactId +
                ", startDate=" + startDate.getTime() +
                ", endDate=" + ((endDate != null) ? endDate.getTime() : null) +
                ", location=" + location +
                '}';
    }
}
