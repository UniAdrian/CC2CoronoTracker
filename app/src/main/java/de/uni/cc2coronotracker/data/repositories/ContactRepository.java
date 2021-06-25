package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class ContactRepository{

    private Context applicationContext;
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


    public LiveData<List<Contact>> getContacts() {
        return contactDao.getAll();
    }


    private Contact getContact(long id) {
        Contact c = contactDao.getByIdSync(id);
        if (c == null) {
            return null;
        }

        Contact detailedContact = readContact(c.lookupUri);
        detailedContact.uuid = c.uuid;

        return detailedContact;
    }

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

    public Contact getContact(UUID uuid) {
        Contact c = contactDao.getByUUIDSync(uuid);
        if (c == null) {
            return null;
        }

        Contact detailedContact = readContact(c.lookupUri);
        detailedContact.uuid = c.uuid;

        return detailedContact;
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

                Result<Contact> result;
                if (c == null) {
                    result = new Result.Success<>(null);
                } else {
                    Contact detailedContact = readContact(c.lookupUri);
                    detailedContact.uuid = c.uuid;
                    result = new Result.Success<>(detailedContact);
                }

                callback.onComplete(result);
            } catch (Exception e) {
                Result<Contact> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    /**
     * Inserts a contact into the database.
     * @param contact The contact to be inserted
     * @return The number of affected rows.
     */
    public long addContact(Contact contact) {
        return contactDao.insert(contact);
    }

    /**
     * Insert a contact into the database asynchronously.
     * @param contact The contact to be inserted
     * @param callback Called when the operation ends.
     */
    public void addContact(Contact contact, RepositoryCallback<Contact> callback) {
        executor.execute(() -> {
            try {
                long generatedId = addContact(contact);
                Log.d("---info---", "Generated ID: " + generatedId);
                if (generatedId > 0) {
                    Result<Contact> result = new Result.Success<>(contactDao.getByUUIDSync(contact.uuid));
                    callback.onComplete(result);
                } else {
                    Result<Contact> errorResult = new Result.Error<>(new SQLException("Failed to insert user."));
                    callback.onComplete(errorResult);
                }
            } catch (Exception e) {
                Log.e("ContactRepository", "Failed to add contact", e);
                Result<Contact> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    /**
     * NUKES The table. Make sure to ask the user if he really wants this before calling this.
     */
    private void deleteAll() {
        contactDao.nukeTable();
    }

    /**
     * NUKES The table. Make sure to ask the user if he really wants this before calling this.
     * Calls the synchronous deleteAll method on the executor and utilizes a callback to communicate
     * with the caller when done.
     * @param callback The callback to be called when done.
     */
    public void deleteAll(RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                deleteAll();
                Result<Void> result = new Result.Success<>(null);
                callback.onComplete(result);
            } catch (Exception e) {
                Log.e("ContactRepository", "Failed to nuke table", e);
                Result<Void> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

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

    public int updateContact(Contact contact) { return contactDao.update(contact); }

    public void updateContact(Contact contact, RepositoryCallback<Contact> callback) {
        executor.execute(() -> {
            try {
                int affectedRows = updateContact(contact);
                if (affectedRows > 0) {
                    Result<Contact> result = new Result.Success<>(readContact(contact.lookupUri));
                    callback.onComplete(result);
                } else {
                    Log.e("ContactRepository", "Failed to update contact. No rows affected.");
                    Result<Contact> errorResult = new Result.Error<>(new SQLException("Failed to update user. No rows affected."));
                    callback.onComplete(errorResult);
                }
            } catch (Exception e) {
                Log.e("ContactRepository", "Failed to update contact", e);
                Result<Contact> errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }


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

    private Contact readContact(@NonNull Uri contactUri) {
        try (Cursor cursor = applicationContext.getContentResolver().query(contactUri, CONTACT_PROJECTION, null, null, null)) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            return contactFromCursor(cursor);
        } catch (Exception e) {
            throw e;
        }
    }

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
}
