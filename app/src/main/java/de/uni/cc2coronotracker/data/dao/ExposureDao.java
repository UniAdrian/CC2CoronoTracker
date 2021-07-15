package de.uni.cc2coronotracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.uni.cc2coronotracker.data.models.Exposure;

@Dao
public abstract class ExposureDao {
    @Query("SELECT * FROM exposures")
    public abstract LiveData<List<Exposure>> getAll();

    @Query("SELECT * FROM exposures WHERE contact_id = :contact_id")
    public abstract LiveData<List<Exposure>> getByContactID(long contact_id);

    @Query("SELECT * FROM exposures WHERE contact_id = :contact_id")
    public abstract List<Exposure> getByContactIDSync(long contact_id);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract long insert(Exposure exposure);

    @Update
    public abstract int update(Exposure exposure);

    @Query("DELETE FROM exposures")
    public abstract void nukeTable();

    @Query("DELETE FROM exposures WHERE id = :id")
    public abstract void delete(long id);

    @Delete
    public abstract void delete(Exposure exposure);

}
