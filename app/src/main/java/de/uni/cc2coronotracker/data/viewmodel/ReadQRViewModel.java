package de.uni.cc2coronotracker.data.viewmodel;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.Event;
import de.uni.cc2coronotracker.helper.RequestFactory;
import de.uni.cc2coronotracker.ui.views.ReadQRFragmentDirections;

@HiltViewModel
public class ReadQRViewModel extends ViewModel {

    private final String TAG = "ReadQRVM";

    private final ContextMediator ctxMediator;
    private final ContactRepository contactRepository;
    private final ExposureRepository exposureRepository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public ReadQRViewModel(ContextMediator ctxMediator, ContactRepository contactRepository, ExposureRepository exposureRepository) {
        this.ctxMediator = ctxMediator;
        this.contactRepository = contactRepository;
        this.exposureRepository = exposureRepository;
    }

    public void handleQRIntent(QrIntent.Intent intent) {
        // TODO: Consider pulling this into an interface method and let java handle the work...
        if (intent instanceof QrIntent.AddExposure) {
            handleAddExposureIntent((QrIntent.AddExposure) intent);
        } else if (intent instanceof QrIntent.ImportSettings) {
            // TODO: Add me!
            ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.unknown_intent, Snackbar.LENGTH_SHORT));
        } else {
            ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.unknown_intent, Snackbar.LENGTH_SHORT));
        }
    }

    public void handleAddExposureIntent(QrIntent.AddExposure intent) {
        contactRepository.getContact(intent.uuid, result -> {
            if (result instanceof Result.Success) {
                Contact c = ((Result.Success<Contact>) result).data;
                if (c == null) {
                    ctxMediator.request(RequestFactory.createContactDialogRequest(false, intent));
                } else {
                    addExposure(c, intent.allowTracking);
                }
            } else {
                Log.e(TAG, "Failed to fetch contact with uuid " + intent.uuid, ((Result.Error)result).exception);
            }
        });
    }


    public void handleContactPick(Event<ContactSelectionDialogViewModel.ContactIntentTuple> event) {
        ContactSelectionDialogViewModel.ContactIntentTuple cit = event.peekContent();
        if (cit == null) return;

        if (cit.intent instanceof QrIntent.AddExposure) {
            ContactSelectionDialogViewModel.ContactIntentTuple tuple = event.getContentIfNotHandled();
            if (tuple != null) {
                QrIntent.AddExposure intent = (QrIntent.AddExposure)tuple.intent;
                connectContactAndAddExposure(tuple.contactList.get(0), intent.uuid, intent.allowTracking);
            }
        }
    }

    private void addExposure(Contact contact, boolean allowTracking) {

        // TODO: Also request and add location data if appropriate. See MapsIntegration milestone

        isLoading.postValue(true);

        Exposure toAdd = new Exposure();
        toAdd.contactId = contact.id;
        toAdd.date = new java.sql.Date(new Date().getTime());

        exposureRepository.addExposure(toAdd, result -> {
            isLoading.postValue(false);
            if (result instanceof Result.Success) {
                // Navigate to the contact that was changed.
                ReadQRFragmentDirections.ActionReadQRToContactDetails actionReadQRToContacts = ReadQRFragmentDirections.actionReadQRToContactDetails();
                actionReadQRToContacts.setContactId(contact.id);

                ctxMediator.request(RequestFactory.createNavigationRequest(actionReadQRToContacts));
            } else {
                Log.e(TAG, "Failed to insert exposure for contact.", ((Result.Error)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.insert_exposure_failed, Snackbar.LENGTH_LONG, R.string.retry, v -> {
                    addExposure(contact, allowTracking);
                }, contact.displayName));
            }
        });


    }

    private void connectContactAndAddExposure(Contact contact, UUID uuid, boolean allowTracking) {
        Log.d(TAG, "AddExposure for " + uuid.toString() + " (" + allowTracking + ") -> " + contact);
        contact.uuid = uuid;
        contactRepository.upsertContact(contact, result -> {
            if (result instanceof Result.Success) {
                addExposure(contact, allowTracking);
            } else {
                Log.e(TAG, "Failed to update contact.", ((Result.Error)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.upsert_contact_failed, Snackbar.LENGTH_LONG, R.string.retry, v -> {
                    connectContactAndAddExposure(contact, uuid, allowTracking);
                }, contact.displayName));
            }
        });
    }


    public LiveData<Boolean> getIsLoading() {return isLoading; }
}
