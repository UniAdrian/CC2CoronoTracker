package de.uni.cc2coronotracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;

@Dao
public abstract class ContactDao {
    @Query("SELECT * FROM contacts ORDER BY display_name ASC")
    public abstract LiveData<List<Contact>> getAll();

    @Query("SELECT * FROM contacts WHERE uuid = :uuid")
    public abstract Contact getByUUIDSync(UUID uuid);

    @Query("SELECT * FROM contacts WHERE id = :id")
    public abstract Contact getByIdSync(long id);

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

    @Transaction
    @Query("SELECT * FROM contacts")
    public abstract LiveData<List<ContactWithExposures>> getAllContactsWithExposures();

    @Transaction
    @Query("SELECT * FROM contacts WHERE id IN (:forContacts)")
    public abstract List<ContactWithExposures> getContactsWithExposuresSync(List<Long> forContacts);

    @Query("DELETE FROM contacts")
    public abstract void nukeTable();

    @Query("DELETE FROM contacts WHERE id = :id")
    public abstract void delete(long id);


    public static class ContactWithExposures {
        @Embedded
        public Contact contact;
        @Relation(
                parentColumn = "id",
                entityColumn = "contact_id"
        )
        public List<Exposure> exposures;
    }
}
