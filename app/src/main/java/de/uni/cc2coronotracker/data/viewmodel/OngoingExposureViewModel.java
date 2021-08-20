package de.uni.cc2coronotracker.data.viewmodel;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Date;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.CallWithContextRequest;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.Event;
import de.uni.cc2coronotracker.helper.RequestFactory;

/**
 * Provides business logic to the {@link de.uni.cc2coronotracker.ui.views.OngoingExposureActivity}
 */
@HiltViewModel
public class OngoingExposureViewModel extends ViewModel {

    private final static String TAG = "OngoingExposureVM";

    private final ExposureRepository exposureRepository;
    private final ContactRepository contactRepository;

    private final ContextMediator ctxMediator;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Exposure> currentExposure = new MutableLiveData<>();
    private final MutableLiveData<Contact> currentContact = new MutableLiveData<>();

    private final MutableLiveData<Event<Boolean>> requestFinish = new MutableLiveData<>();

    @Inject
    public OngoingExposureViewModel(ExposureRepository exposureRepository, ContactRepository contactRepository, ContextMediator ctxMediator) {
        this.exposureRepository = exposureRepository;
        this.contactRepository = contactRepository;
        this.ctxMediator = ctxMediator;
    }

    /**
     * Prepares the vm for the given exposureId
     * @param exposureId The unique ID of the current exposure
     */
    public void setExposure(long exposureId) {
        this.isLoading.postValue(true);
        this.exposureRepository.getExposure(exposureId, result -> {

            if (result instanceof Result.Success) {
                Exposure current = ((Result.Success<Exposure>) result).data;
                currentExposure.postValue(current);
                setContact(current.contactId);
            } else {
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.exposure_fetch_failed, Snackbar.LENGTH_LONG, R.string.retry, view -> setExposure(exposureId)));
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Fetches the user associated with the exposureId
     * @param contactId
     */
    private void setContact(long contactId) {
        this.isLoading.postValue(true);
        this.contactRepository.getContact(contactId, result -> {

            if (result instanceof Result.Success) {
                Contact current = ((Result.Success<Contact>) result).data;
                currentContact.postValue(current);
            } else {
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.fetch_contact_failed, Snackbar.LENGTH_LONG, R.string.retry, view -> setContact(contactId)));
            }
            isLoading.postValue(false);
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<Exposure> currentExposure() {
        return currentExposure;
    }
    public LiveData<Contact> currentContact() {
        return currentContact;
    }

    /**
     * @return A list of request events to finish the activity.
     */
    public LiveData<Event<Boolean>> getRequestFinish() { return requestFinish; }

    /**
     * If the current exposure is valid stores it and finishes the activity.
     */
    public void finalizeCurrentExposure() {
        Exposure current = currentExposure.getValue();
        if (current == null || current.endDate != null) return;

        updateExposureAndBail(current);
    }

    /**
     * Updates the current exposure and stores it in the repository
     * @param exposure The exposure to be updated
     */
    private void updateExposureAndBail(Exposure exposure) {
        exposure.endDate = new Date(new java.util.Date().getTime());
        exposureRepository.updateExposure(exposure, result -> {
            if (result instanceof Result.Success) {

                // Also navigate to the users details page. :)
                ctxMediator.request(new CallWithContextRequest(c -> {
                    Bundle args = new Bundle();
                    args.putLong("contactId", exposure.contactId);
                    Navigation.findNavController((AppCompatActivity) c, R.id.nav_host_fragment).navigate(R.id.contactDetailsFragment, args);
                }));

                // This will automatically be called in the old activity, nice hu? :)
                // inform the user of success...
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.exposure_update_success, Snackbar.LENGTH_SHORT));
            } else {
                Log.e(TAG, "Failed to update exposure", ((Result.Error<Void>)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.update_exposure_failed, Snackbar.LENGTH_LONG, R.string.retry, view -> finalizeCurrentExposure()));
            }

            // Request finish of this activity regardless.
            requestFinish.postValue(new Event<>(true));
            isLoading.postValue(false);
        });
    }

    /**
     * Called via a deeplink most likely.
     * @param exposureId The exposure id to finalize.
     */
    public void endExposureById(long exposureId) {
        isLoading.postValue(true);
        this.exposureRepository.getExposure(exposureId, result -> {
            if (result instanceof Result.Success) {
                Exposure current = ((Result.Success<Exposure>) result).data;
                updateExposureAndBail(current);
            } else {
                Log.e(TAG, "Failed to fetch exposure", ((Result.Error<Exposure>)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.exposure_fetch_failed, Snackbar.LENGTH_LONG, R.string.retry, view -> setExposure(exposureId)));
            }
        });
    }
}
