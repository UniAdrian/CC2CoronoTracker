package de.uni.cc2coronotracker.data.dao;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import de.uni.cc2coronotracker.data.models.Contact;

@Dao
public abstract class ContactDao {
    @Query("SELECT * FROM contacts ORDER BY display_name ASC")
    public abstract LiveData<List<Contact>> getAll();

    @Query("SELECT * FROM contacts WHERE uuid = :uuid")
    public abstract LiveData<Contact> getByUUID(UUID uuid);

    @Query("SELECT * FROM contacts WHERE uuid = :uuid")
    public abstract Contact getByUUIDSync(UUID uuid);

    @Query("SELECT * FROM contacts WHERE id = :id")
    public abstract Contact getByIdSync(long id);

    @Query("SELECT * FROM contacts WHERE uuid IN (:uuids)")
    public abstract LiveData<List<Contact>> getAllByIds(UUID[] uuids);

    @Query("SELECT * FROM contacts WHERE lookup_uri = :lookupUri")
    public abstract LiveData<Contact> findByLookUpUri(Uri lookupUri);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Contact contact);

    @Update
    public abstract int update(Contact contact);

    @Transaction
    public boolean upsert(Contact contact) {
        long id = insert(contact);
        if (id == -1) {
            update(contact);
            return true;
        }
        return false;
    }

    @Query("DELETE FROM contacts")
    public abstract void nukeTable();
}
