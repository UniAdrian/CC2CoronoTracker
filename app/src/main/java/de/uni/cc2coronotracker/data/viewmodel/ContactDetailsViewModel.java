package de.uni.cc2coronotracker.data.viewmodel;

import android.content.DialogInterface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class ContactDetailsViewModel extends ViewModel {

    private final String LOG_TAG = "ContactDetailsVM";

    private final ContactRepository contactRepository;
    private final ExposureRepository exposureRepository;
    private final ContextMediator ctxMediator;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private MutableLiveData<Contact> contact = new MutableLiveData<>();
    private MutableLiveData<List<Exposure>> exposures = new MutableLiveData<>();


    @Inject
    public ContactDetailsViewModel(@NonNull SavedStateHandle savedState, @NonNull ContactRepository contactRepository, @NonNull ExposureRepository exposureRepository, @NonNull ContextMediator ctxMediator) {
        this.contactRepository = contactRepository;
        this.exposureRepository = exposureRepository;
        this.ctxMediator = ctxMediator;
    }

    public void setContactId(long contactId) {
        isLoading.postValue(true);

        contactRepository.getContact(contactId, queryResult -> {
            if (queryResult instanceof Result.Success) {
                Log.d(LOG_TAG, "Fetched contact: " + ((Result.Success<Contact>) queryResult).data);
                Contact receivedContact = ((Result.Success<Contact>) queryResult).data;
                contact.postValue(receivedContact);

                exposureRepository.getExposures(receivedContact, (exposureResult) -> {
                    isLoading.postValue(false);

                    if (exposureResult instanceof Result.Success) {
                        exposures.postValue(((Result.Success<List<Exposure>>) exposureResult).data);
                    } else {
                        exposures.postValue(null);
                        Exception e = ((Result.Error) exposureResult).exception;
                        Log.e(LOG_TAG, "Failed to fetch exposures.", e);
                    }

                });


            } else {
                Exception e = ((Result.Error<Contact>) queryResult).exception;
                Log.e(LOG_TAG, "Failed to fetch contact.", e);
                ctxMediator.request(RequestFactory.createNavigationRequest(R.id.action_contactDetailsFragment_to_contacts));
                isLoading.postValue(false);
            }
        });
    }

    public void deleteContact() {
        Contact currentContact = contact.getValue();
        if (currentContact == null) return;

        ctxMediator.request(RequestFactory.createConfirmationDialogRequest(R.string.confirm_contact_deletion, R.string.confirm_contact_deletion_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isLoading.postValue(true);
                contactRepository.delete(currentContact.id, result -> {
                    if (result instanceof Result.Success) {
                        ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.delete_contact_success, Snackbar.LENGTH_SHORT));
                        ctxMediator.request(RequestFactory.createNavigationRequest(R.id.action_contactDetailsFragment_to_contacts));
                    } else {
                        ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.delete_contact_failure, Snackbar.LENGTH_SHORT, R.string.retry, (v) -> {
                            deleteContact();
                        }));
                    }
                    isLoading.postValue(false);
                });
            }
        }, null, currentContact.displayName));
    }

    public LiveData<Boolean> getLoading() {
        return isLoading;
    }
    public LiveData<Contact> getContact() { return contact; }
    public LiveData<List<Exposure>> getExposures() {
        return exposures;
    }

}
