package de.uni.cc2coronotracker.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.uni.cc2coronotracker.data.dao.CertificateDao;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.dao.ExposureDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.qr.EGC;

@Database(entities = {Contact.class, Exposure.class, EGC.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao getContactDao();
    public abstract ExposureDao getExposureDao();
    public abstract CertificateDao getCertificateDao();
}
