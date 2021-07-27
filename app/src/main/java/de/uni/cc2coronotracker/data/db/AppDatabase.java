package de.uni.cc2coronotracker.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.dao.ExposureDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;

@Database(entities = {Contact.class, Exposure.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao getContactDao();
    public abstract ExposureDao getExposureDao();
}
