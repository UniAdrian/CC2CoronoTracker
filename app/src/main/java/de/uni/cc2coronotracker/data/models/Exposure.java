package de.uni.cc2coronotracker.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

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
    @NonNull
    public long id;

    @ColumnInfo(name = "contact_id", index = true)
    @NonNull
    public long contactId;

    @ColumnInfo(name = "date")
    @NonNull
    public Date date;
}
