package de.uni.cc2coronotracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.uni.cc2coronotracker.data.models.CertEntity;

@Dao
public abstract class CertificateDao {
    @Query("SELECT * FROM certificates")
    public abstract LiveData<List<CertEntity>> getAll();

    @Query("SELECT * FROM certificates WHERE identifier = :identifier")
    public abstract CertEntity getCert(String identifier);

    @Insert
    public abstract long insertCert(CertEntity ent);
}
