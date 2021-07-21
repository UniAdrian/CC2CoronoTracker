package de.uni.cc2coronotracker.data.viewmodel;

import android.Manifest;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.Event;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class ContactViewModel extends ViewModel {

    private final ContactRepository contactRepository;
    private final ContextMediator ctxMediator;

    private LiveData<List<ContactDao.ContactWithExposures>> allContactsWithExposures;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Event<Void>> requestContactPick = new MutableLiveData<>();

    @Inject
    public ContactViewModel(ContactRepository contactRepository, ContextMediator ctxMediator) {
        this.contactRepository = contactRepository;
        this.ctxMediator = ctxMediator;

        loading.setValue(true);
        this.allContactsWithExposures = Transformations.map(contactRepository.getContactsWithExposures(), list -> {
            loading.setValue(false);
            return list;
        });
    }

    public LiveData<List<ContactDao.ContactWithExposures>> getAllContactsWithExposures() {
        return allContactsWithExposures;
    }

    public LiveData<Event<Void>> getRequestContactPick() { return requestContactPick; }

    public LiveData<Boolean> isLoading() {
        return loading;
    }


    public void importPhoneContact() {
        ctxMediator.request(RequestFactory.createPermissionRequest(Manifest.permission.READ_CONTACTS, new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                requestContactPick.postValue(new Event<>(null));
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                ctxMediator.request(RequestFactory.createSnackbarRequest(
                        R.string.no_permission_no_import,
                        Snackbar.LENGTH_LONG,
                        R.string.retry, v -> importPhoneContacts())
                );
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }));
    }

    public void importPhoneContacts() {
        loading.postValue(true);

        ctxMediator.request(RequestFactory.createPermissionRequest(Manifest.permission.READ_CONTACTS, new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                contactRepository.importContacts( result -> {
                    if (result instanceof Result.Success) {
                        Log.d("Contacts", "Imported all contacts!");
                        loading.postValue(false);
                    } else {
                        Log.e("Contacts", "Failed to import phone contacts", ((Result.Error)result).exception);
                        showImportError();
                        loading.postValue(false);
                    }
                });
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                loading.postValue(false);
                ctxMediator.request(RequestFactory.createSnackbarRequest(
                        R.string.no_permission_no_import,
                        Snackbar.LENGTH_LONG,
                        R.string.retry, v -> importPhoneContacts())
                );
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }));
    }

    private void showImportError() {
        ctxMediator.request(RequestFactory.createSnackbarRequest(
                R.string.import_contact_failed,
                Snackbar.LENGTH_LONG,
                R.string.retry, v -> importPhoneContacts())
        );
    }

    public void addContact() {

    }

    public void clearDatabase() {
        contactRepository.deleteAll(result -> {
            if (result instanceof Result.Success) {
                Log.d("Contacts", "Nuked database.");
            } else {
                Log.e("Contacts", "Failed to nuke database.", ((Result.Error)result).exception);
            }
        });
    }

    /**
     * Called when the user picked/selected a single contact to import
     * @param uri The lookup uri of the picked user.
     */
    public void onContactPick(@Nullable Uri uri) {
        if (uri == null) return;

        this.loading.postValue(true);
        contactRepository.readContact(uri, result -> {
            if (result instanceof Result.Success) {
                Contact contact = ((Result.Success<Contact>) result).data;
                contactRepository.upsertContact(contact, innerResult -> {
                    if (result instanceof Result.Success) {
                        ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.contact_import_success, Snackbar.LENGTH_SHORT));
                    } else {
                        Log.e("Contacts", "DB Insert failed.", ((Result.Error)result).exception);
                        ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.import_contact_failed, Snackbar.LENGTH_SHORT));
                    }
                    this.loading.postValue(false);
                });
            } else {
                Log.e("Contacts", "Import failed.", ((Result.Error)result).exception);
                this.loading.postValue(false);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.import_contact_failed, Snackbar.LENGTH_SHORT));
            }
        });
    }
}
