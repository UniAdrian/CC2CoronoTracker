package de.uni.cc2coronotracker.data.viewmodel;


import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Location;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.qr.EGC;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.repositories.CertificateRepository;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.data.repositories.providers.ReadOnlySettingsProvider;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
import de.uni.cc2coronotracker.helper.CallWithContextRequest;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.Event;
import de.uni.cc2coronotracker.helper.RequestFactory;
import de.uni.cc2coronotracker.ui.views.OngoingExposureActivity;
import de.uni.cc2coronotracker.ui.views.ReadQRFragmentDirections;

/**
 * Provides the business logic for the QR scanning screen.
 * Parses, prepares and forwards valid QR intents.
 */
@HiltViewModel
public class ReadQRViewModel extends ViewModel {

    private final String TAG = "ReadQRVM";

    private final ContextMediator ctxMediator;
    private final LocationProvider locationprovider;
    private final ReadOnlySettingsProvider settingsProvider;

    private final ContactRepository contactRepository;
    private final ExposureRepository exposureRepository;
    private final CertificateRepository certificateRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> isLoadingText = new MutableLiveData<>();

    @Inject
    public ReadQRViewModel(ContextMediator ctxMediator, ContactRepository contactRepository,
                           ExposureRepository exposureRepository, LocationProvider locationProvider,
                           ReadOnlySettingsProvider settingsProvider, CertificateRepository certificateRepository) {
        this.ctxMediator = ctxMediator;
        this.locationprovider = locationProvider;
        this.settingsProvider = settingsProvider;
        this.contactRepository = contactRepository;
        this.exposureRepository = exposureRepository;
        this.certificateRepository = certificateRepository;
    }

    /**
     * Given a valid QR intent does the necessary work and forwards the data/user to corresponding fragments.
     * @param intent
     */
    public void handleQRIntent(QrIntent.Intent intent) {
        if (intent instanceof QrIntent.AddExposure) {
            handleAddExposureIntent((QrIntent.AddExposure) intent);
        } else if (intent instanceof QrIntent.ImportSettings) {
            // TODO: Add me! I am a bonus feature.
            ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.unknown_intent, Snackbar.LENGTH_SHORT));
        } else if (intent instanceof EGC) {
            handleEGCIntent((EGC)intent);
        } else {
            ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.unknown_intent, Snackbar.LENGTH_SHORT));
        }
    }

    /**
     * Called when a valid EGC intent (aka a valid eu health certificate) is scanned.
     * Stores the certificate and forwards the user to its details page.
     * @param intent The valid EGC intent.
     */
    private void handleEGCIntent(EGC intent) {
        isLoading.postValue(true);
        certificateRepository.addEGC(intent, result -> {
            if (result instanceof Result.Success) {
                long generatedId = ((Result.Success<Long>) result).data;

                ReadQRFragmentDirections.ActionReadQRToCertificateFragment actionGotoCertificates = ReadQRFragmentDirections.actionReadQRToCertificateFragment(intent);
                ctxMediator.request(RequestFactory.createNavigationRequest(actionGotoCertificates));

                Log.d(TAG, "Inserted new Certificate with id " + generatedId);
            } else {
                Exception e = ((Result.Error<Long>)result).exception;
                // if it is just a unique constraint violation we simply already have this cert stored
                // in this case we give the user the option to go there via the snackbar.
                if (e instanceof SQLiteConstraintException) {
                    ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.insert_certificate_failed_already_exists, Snackbar.LENGTH_LONG, R.string.goto_certificate_on_exists, v -> gotoCert(intent)));
                } else {
                    Log.e(TAG, "Failed to insert new Certificate.", e);
                    ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.insert_certificate_failed, Snackbar.LENGTH_LONG, R.string.retry, v -> handleEGCIntent(intent)));
                }
            }
            isLoading.postValue(false);
        });
    }

    /**
     * Navigates the user to the given certs details page.
     * @param intent The certificate to display
     */
    private void gotoCert(EGC intent) {
        isLoading.postValue(true);
        certificateRepository.getByIdentifier("", result -> {
            if (result instanceof Result.Success) {
                ReadQRFragmentDirections.ActionReadQRToCertificateFragment actionGotoCertificates = ReadQRFragmentDirections.actionReadQRToCertificateFragment(intent);
                ctxMediator.request(RequestFactory.createNavigationRequest(actionGotoCertificates));
            } else {
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.goto_certificate_failed, Snackbar.LENGTH_LONG, R.string.retry, v -> gotoCert(intent)));
            }
            isLoading.postValue(false);
        });
    }

    /**
     * Called when a valid add exposure intent is scanned.
     * If no user is associated with the scanned UUID yet the user is asked to associate it with a
     * user before proceeding.
     * Then the ongoing exposure activity is opened for the user and the exposure added to the repo.
     * @param intent The valid AddExposure intent scanned by the user.
     */
    public void handleAddExposureIntent(QrIntent.AddExposure intent) {
        contactRepository.getContact(intent.uuid, result -> {
            if (result instanceof Result.Success) {
                Contact c = ((Result.Success<Contact>) result).data;
                if (c == null) {
                    ctxMediator.request(RequestFactory.createContactSelectionDialogRequest(false, intent));
                } else {
                    prepareAndAddExposure(c, intent.allowTracking);
                }
            } else {
                Log.e(TAG, "Failed to fetch contact with uuid " + intent.uuid, ((Result.Error<?>)result).exception);
            }
        });
    }

    /**
     * Called when a user was prompted to select a user for a given Intent.
     * @param event The picked contact and the associated intent.
     * @apiNote Currently only supports th AddExposure intent and must be expanded upon if new features
     * are implemented.
     */
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

    /**
     * Adds the exposure to the repository and starts the ongoing exposure activity.
     * @param toAdd The exposure to be added.
     */
    private void addExposure(Exposure toAdd) {
        isLoadingText.postValue("One last step...");
        exposureRepository.addExposure(toAdd, result -> {
            isLoading.postValue(false);

            if (result instanceof Result.Success) {

                // Start the ongoing exposure Activity. As usual simply request it.
                ctxMediator.request(new CallWithContextRequest(c -> {
                    AppCompatActivity activity = (AppCompatActivity)c;
                    Intent intent = new Intent(activity, OngoingExposureActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("exposure_id", ((Result.Success<Long>) result).data);
                    activity.startActivity(intent);
                }));
            } else {
                Log.e(TAG, "Failed to insert exposure for contact.", ((Result.Error<?>)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.insert_exposure_failed, Snackbar.LENGTH_LONG, R.string.retry, v -> addExposure(toAdd)));
            }
        });
    }


    /**
     * Prepares a new exposure (including location data if configured to do so and the other party allows it)
     * Then calls <ref>this.addExposure()</ref>
     * @param contact The contact to prepare a new exposure with
     * @param allowTracking Wether or not the contact wants to be tracked
     */
    private void prepareAndAddExposure(Contact contact, boolean allowTracking) {
        isLoading.postValue(true);
        isLoadingText.postValue("Preparing exposure...");

        Exposure toAdd = new Exposure();
        toAdd.contactId = contact.id;
        toAdd.location = null;
        toAdd.startDate = new java.sql.Date(new Date().getTime());


        // does the user want to be tracked and we allow it ourselves?
        if (!allowTracking || !settingsProvider.getTrackExposures()) {
            addExposure(toAdd);
            return;
        }

        isLoadingText.postValue("Waiting for location data...");
        LocationProvider.LocationListener locationListener = new LocationProvider.LocationListener() {
            @Override
            public void onLocation(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                toAdd.location = new LatLng(location.getLatitude(), location.getLongitude());

                locationprovider.removeLocationListener(this);
                addExposure(toAdd);
            }

            @Override
            public void onLocationUnavailable() {
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.allow_location_or_preferences, Snackbar.LENGTH_LONG));
                locationprovider.removeLocationListener(this);
                isLoading.postValue(false);
            }
        };

        locationprovider.addLocationListener(locationListener);
    }

    /**
     * Associates a contact with a UUID, then calls <ref>prepareAndAddExposure</ref>
     * @param contact The contact to be associated with the UUID
     * @param uuid The UUID to be associated with the contact
     * @param allowTracking Wether or not the user allows tracking.
     */
    private void connectContactAndAddExposure(Contact contact, UUID uuid, boolean allowTracking) {
        Log.d(TAG, "AddExposure for " + uuid.toString() + " (" + allowTracking + ") -> " + contact);
        contact.uuid = uuid;
        contactRepository.upsertContact(contact, result -> {
            if (result instanceof Result.Success) {
                prepareAndAddExposure(contact, allowTracking);
            } else {
                Log.e(TAG, "Failed to update contact.", ((Result.Error<?>)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.upsert_contact_failed, Snackbar.LENGTH_LONG, R.string.retry, v -> connectContactAndAddExposure(contact, uuid, allowTracking), contact.displayName));
            }
        });
    }


    public LiveData<Boolean> isLoading() {return isLoading; }
    public LiveData<String> getLoadingText() {return isLoadingText; }
}
