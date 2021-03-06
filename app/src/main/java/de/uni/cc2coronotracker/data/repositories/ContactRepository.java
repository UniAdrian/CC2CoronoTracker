package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

/**
 * Single source of truth concerning Contact data.
 */
public class ContactRepository{

    private final Context applicationContext;
    private final Executor executor;

    private final ContactDao contactDao;

    final String[] CONTACT_PROJECTION = new String[]{
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
    };

    @Inject
    public ContactRepository(@ApplicationContext Context ctx, Executor executor, ContactDao contactDao) {
        applicationContext = ctx;
        this.executor = executor;
        this.contactDao = contactDao;
    }

    /**
     *
     * @return A LiveData instance of a list containing all contacts with its respective exposures.
     */
    public LiveData<List<ContactDao.ContactWithExposures>> getContactsWithExposures() {
        return contactDao.getAllContactsWithExposures();
    }

    /**
     * Fetches all {@link de.uni.cc2coronotracker.data.dao.ContactDao.ContactWithExposures} for the given contacts.
     * @param forContacts The list of contacts to be fetched
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void getContactsWithExposures(List<Contact> forContacts, RepositoryCallback<List<ContactDao.ContactWithExposures>> callback) {
        executor.execute(() -> {

            try {
                // I really miss my streams...
                List<Long> asIdList = new ArrayList<>(forContacts.size());
                for (Contact contact : forContacts) {
                    asIdList.add(contact.id);
                }

                List<ContactDao.ContactWithExposures> contactsWithExposuresResult= contactDao.getContactsWithExposuresSync(asIdList);
                callback.onComplete(new Result.Success<>(contactsWithExposuresResult));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Fetches a single contact by unique id
     * @param id The contacts unique id
     * @return The contact fetched
     */
    private Contact getContact(long id) {
        return contactDao.getByIdSync(id);
    }

    public Contact getContactSync(long id) {
        return getContact(id);
    }

    /**
     * Fetches a contact from the database by unique ID
     * @param id The unique contact ID
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void getContact(long id, RepositoryCallback<Contact> callback) {
        executor.execute(() -> {
            try {
                Contact c = getContact(id);
                callback.onComplete(new Result.Success<>(c));
            } catch (Exception e) {
                Result<Contact> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    /**
     * Searches the DB for the user with the given UUID asynchronously and calls a callback when done.
     * @param uuid The UUID of to search for
     * @param callback The callback to be called when done
     */
    public void getContact(UUID uuid, RepositoryCallback<Contact> callback) {
        executor.execute(() -> {
            try {
                Contact c = contactDao.getByUUIDSync(uuid);
                callback.onComplete(new Result.Success<>(c));
            } catch (Exception e) {
                Result<Contact> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    /**
     * Creates OR updates a contact
     * @param c The contact to be inserted
     * @return True on insert, false on update
     */
    private boolean upsertContact(Contact c) {return contactDao.upsert(c);}
    public void upsertContact(Contact c, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(upsertContact(c)));
            } catch (Exception e) {
                Log.e("ContactRepository", "Failed to upsert contact", e);
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Inserts a contact into the database
     * @param c The contact to be inserted
     * @return The id of the new entry
     */
    private long insertContact(Contact c) {return contactDao.insert(c);}
    public void insertContact(Contact c, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(insertContact(c)));
            } catch (Exception e) {
                Log.e("ContactRepository", "Failed to insert contact", e);
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Imports all contacts from the phones contact list and stores it in the local database.
     * Fetches and stores all fields in the {@link ContactRepository#CONTACT_PROJECTION}
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void importContacts(RepositoryCallback<Void> callback) {
        executor.execute( () -> {
            // Try with resources is your friend when working with cursors. No leaks guaranteed.
            try (Cursor cursor = applicationContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, CONTACT_PROJECTION, null, null, null)) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        Contact currentContact = contactFromCursor(cursor);
                        contactDao.upsert(currentContact);
                    }
                }

                Result<Void> errorResult = new Result.Success<>(null);
                callback.onComplete(errorResult);
            } catch (Exception e) {
                Log.e("ContactRepository", "Failed to import phone contacts", e);
                Result<Void> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    /**
     * Given a valid contact Uri, reads the data from the phone contact and creates a Contact instance
     * with the retrieved data.
     * @param contactUri The URI of the phone contact
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void readContact(@NonNull Uri contactUri, RepositoryCallback<Contact> callback) {
        executor.execute(() -> {
            try {
                Contact c = readContact(contactUri);
                callback.onComplete(new Result.Success<>(c));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Given a valid contact Uri, reads the data from the phone contact and creates a Contact instance
     * with the retrieved data.
     * @param contactUri
     * @return The contact
     */
    private Contact readContact(@NonNull Uri contactUri) {
        try (Cursor cursor = applicationContext.getContentResolver().query(contactUri, CONTACT_PROJECTION, null, null, null)) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            return contactFromCursor(cursor);
        }
    }

    /**
     * Given a valid cursor to a phone contact fetches and stores all projected fields in a new contact
     * instance
     * @param cursor The cursor to a phone contact
     * @return The created contact instance
     */
    private Contact contactFromCursor(@NonNull Cursor cursor) {
        // We have a valid cursor.
        int lookupIndex = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int starredIndex = cursor.getColumnIndex(ContactsContract.Contacts.STARRED);

        int photoIndex;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            photoIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        } else {
            photoIndex = idIndex;
        }

        Contact result = new Contact();
        result.lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(idIndex), cursor.getString(lookupIndex));
        result.displayName = cursor.getString(nameIndex);
        result.favorite = cursor.getInt(starredIndex) == 1;
        result.photoUri = null;

        String photouri = cursor.getString(photoIndex);
        if (photouri != null) {
            result.photoUri = Uri.parse(photouri);
        }
        return result;
    }

    /**
     * Deletes a contact by unique ID
     * @param id The unique id of the contact
     */
    private void delete(long id) {
        contactDao.delete(id);
    }

    /**
     * Deletes a contact by unique ID
     * @param id The unique id of the contact
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void delete(long id, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                delete(id);
                callback.onComplete(new Result.Success<>(null));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }


    /**
     * Updates the contact to the new values
     * @param toUpdate The contact with new values.
     */
    public void update(Contact toUpdate, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(contactDao.update(toUpdate)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
