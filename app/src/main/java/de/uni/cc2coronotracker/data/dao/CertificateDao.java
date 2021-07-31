package de.uni.cc2coronotracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.uni.cc2coronotracker.data.qr.EGC;

@Dao
public abstract class CertificateDao {
    @Transaction
    @Query("SELECT * FROM certificates WHERE id = :id")
    public abstract List<EGC> getOne(long id);

    @Insert
    public abstract long add(EGC toAdd);
}
