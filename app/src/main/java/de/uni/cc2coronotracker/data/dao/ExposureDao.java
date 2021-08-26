package de.uni.cc2coronotracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.sql.Date;
import java.util.List;

import de.uni.cc2coronotracker.data.models.Exposure;

@Dao
public abstract class ExposureDao {
    @Query("SELECT * FROM exposures ORDER BY start_date DESC")
    public abstract List<Exposure> getAll();

    @Query("SELECT * FROM exposures WHERE contact_id = :contact_id")
    public abstract List<Exposure> getByContactIDSync(long contact_id);

    @Query("SELECT * FROM exposures WHERE id = :exposure_id")
    public abstract Exposure getById(long exposure_id);

    @Insert()
    public abstract long insert(Exposure exposure);

    @Query("SELECT * FROM exposures WHERE start_date >= :date AND start_date < (:date + 86400000)")
    public abstract List<Exposure> getExposuresByDate(Date date);

    @Query("SELECT * FROM exposures WHERE start_date >= :leastDate")
    public abstract List<Exposure> getExposuresAfter(Date leastDate);

    @Update()
    public abstract void update(Exposure exposure);
}
