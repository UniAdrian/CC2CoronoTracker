package de.uni.cc2coronotracker.data.viewmodel;

import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.data.repositories.providers.ReadOnlySettingsProvider;
import de.uni.cc2coronotracker.helper.CallWithContextRequest;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;
import de.uni.cc2coronotracker.ui.views.OngoingExposureActivity;

/**
 * Provides the logic for the contact details screen.
 */
@HiltViewModel
public class ContactDetailsViewModel extends ViewModel {

    private final String TAG = "ContactDetailsVM";

    private final ContactRepository contactRepository;
    private final ExposureRepository exposureRepository;
    private final ContextMediator ctxMediator;

    private final LocationProvider locationprovider;
    private final ReadOnlySettingsProvider settingsProvider;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final MutableLiveData<Contact> contact = new MutableLiveData<>();
    private final MutableLiveData<List<Exposure>> exposures = new MutableLiveData<>();


    @Inject
    public ContactDetailsViewModel(@NonNull ContactRepository contactRepository,
                                   @NonNull ExposureRepository exposureRepository,
                                   @NonNull ContextMediator ctxMediator,
                                   @NonNull LocationProvider locationProvider,
                                   @NonNull ReadOnlySettingsProvider settingsProvider) {
        this.contactRepository = contactRepository;
        this.exposureRepository = exposureRepository;
        this.ctxMediator = ctxMediator;
        this.locationprovider = locationProvider;
        this.settingsProvider = settingsProvider;
    }

    /**
     * Called initially, fetches the users information and makes them available for the fragment.
     * @param contactId The id of the user to fetch details for.
     */
    public void setContactId(long contactId) {
        isLoading.postValue(true);

        contactRepository.getContact(contactId, queryResult -> {
            if (queryResult instanceof Result.Success) {
                Log.d(TAG, "Fetched contact: " + ((Result.Success<Contact>) queryResult).data);
                Contact receivedContact = ((Result.Success<Contact>) queryResult).data;
                contact.postValue(receivedContact);

                exposureRepository.getExposures(receivedContact, (exposureResult) -> {
                    isLoading.postValue(false);

                    if (exposureResult instanceof Result.Success) {
                        exposures.postValue(((Result.Success<List<Exposure>>) exposureResult).data);
                    } else {
                        exposures.postValue(null);
                        Exception e = ((Result.Error<?>) exposureResult).exception;
                        Log.e(TAG, "Failed to fetch exposures.", e);
                    }

                });


            } else {
                Exception e = ((Result.Error<Contact>) queryResult).exception;
                Log.e(TAG, "Failed to fetch contact.", e);
                ctxMediator.request(RequestFactory.createNavigationRequest(R.id.action_contactDetailsFragment_to_contacts));
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Deletes the current contact if set, does nothing otherwise.
     * @apiNote If this runs successfully the user is navigated to the contacts fragment automatically.
     */
    public void deleteContact() {
        Contact currentContact = contact.getValue();
        if (currentContact == null) return;

        ctxMediator.request(RequestFactory.createConfirmationDialogRequest(R.string.confirm_contact_deletion, R.string.confirm_contact_deletion_title, (dialog, which) -> {
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
        }, null, currentContact.displayName));
    }

    /**
     * Prepares an exposure and opens the ongoing exposure activity if a valid user is currently set.
     */
    public void checkInManual() {
        Contact currentContact = contact.getValue();
        if (currentContact == null) return;

        prepareAndAddExposure(currentContact);
    }

    /**
     * Adds a new exposure to the database
     * @param toAdd The exposure to add.
     */
    private void addExposure(Exposure toAdd) {
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
     * Creates a new exposure and adds it to the repository for a given contact.
     * If configured to track lcoation data it also requests and adds the location data.
     * @param contact The contact to add a new exposure for.
     */
    private void prepareAndAddExposure(Contact contact) {
        isLoading.postValue(true);

        Exposure toAdd = new Exposure();
        toAdd.contactId = contact.id;
        toAdd.location = null;
        toAdd.startDate = new java.sql.Date(new Date().getTime());

        // if we do not allow tracking of location data we proceed without it...
        if (!settingsProvider.getTrackExposures()) {
            addExposure(toAdd);
            return;
        }


        // otherwise we use the locationprovider to fetch the current location data and then add the exposure.
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
     * Provides the ui a possibility to react to loading states.
     * @return True if the viewmodel is currently waiting for asynchronous tasks to finish, false otherwise
     */
    public LiveData<Boolean> getLoading() {
        return isLoading;
    }

    /**
     * @return The current contact or null
     */
    public LiveData<Contact> getContact() { return contact; }

    /**
     * @return The current contacts exposures or null
     */
    public LiveData<List<Exposure>> getExposures() {
        return exposures;
    }

}
