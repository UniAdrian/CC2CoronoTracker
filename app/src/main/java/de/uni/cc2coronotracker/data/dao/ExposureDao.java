package de.uni.cc2coronotracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import de.uni.cc2coronotracker.data.models.Exposure;

@Dao
public abstract class ExposureDao {
    @Query("SELECT * FROM exposures")
    public abstract LiveData<List<Exposure>> getAll();

    @Query("SELECT * FROM exposures WHERE contact_id = :contact_id")
    public abstract List<Exposure> getByContactIDSync(long contact_id);

    public abstract long insert(Exposure exposure);

    @Query("SELECT COUNT(*) FROM exposures WHERE contact_id = :contactId")
    public abstract long getNumberOfExposuresFor(long contactId);
}
