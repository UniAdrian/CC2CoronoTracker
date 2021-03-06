package de.uni.cc2coronotracker.data.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.api.RKIApiInterface;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.models.api.Attributes;
import de.uni.cc2coronotracker.data.models.api.RKIApiResult;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Provides the map views business logic
 */
@HiltViewModel
public class MapsViewModel extends ViewModel {

    private final String TAG = "MapsViewModel";

    private final MutableLiveData<LatLng> requestPosition = new MutableLiveData<>();
    private final MutableLiveData<List<MarkerOptions>> markers = new MutableLiveData<>();

    public final MutableLiveData<LatLng> ownLocation = new MutableLiveData<>(null);
    private final MutableLiveData<LocationInfo> locationInfos = new MutableLiveData<>(null);

    private final MutableLiveData<List<Contact>> selectedContacts = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final LocationProvider locationProvider;
    private final ContactRepository contactRepository;
    private final RKIApiInterface api;

    private final Executor executor;
    private final ContextMediator ctxMediator;

    @Inject
    public MapsViewModel(ContextMediator mediator, LocationProvider locationProvider,
                         ContactRepository contactRepository, Executor executor, RKIApiInterface api) {
        ctxMediator = mediator;
        this.executor = executor;
        this.locationProvider = locationProvider;
        this.contactRepository = contactRepository;
        this.api = api;
    }


    public LiveData<List<Contact>> getSelectedContacts() {return selectedContacts; }
    public LiveData<LatLng> getGotoPosition() {
        return requestPosition;
    }
    public LiveData<List<MarkerOptions>> getMarkers() {
        return markers;
    }
    public LiveData<Boolean> getIsLoading() {return isLoading; }
    public LiveData<LocationInfo> getLocationInfo() { return locationInfos; }


    /**
     * Called once the map is available. All logic starts from here.
     */
    public void onMapReady() {
        locationProvider.getLastLocation(result -> {
            if (result instanceof Result.Success) {
                Location loc = ((Result.Success<Location>) result).data;
                requestPosition.postValue(new LatLng(loc.getLatitude(), loc.getLongitude()));
            } else {
                Log.e(TAG, "Failed to fetch location data", ((Result.Error<?>)result).exception);
                // ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.location_not_available, Snackbar.LENGTH_LONG));
            }
        });
    }

    /**
     * starts listening to location updates.
     * @see {@link LocationProvider}
     */
    public void startUpdateOwnLocation() {
        locationProvider.addLocationListener(ownLocationListener);
    }

    /**
     * Stops listening to location updates.
     * @see {@link LocationProvider}
     */
    public void stopUpdateOwnLocation() {
        locationProvider.removeLocationListener(ownLocationListener);
    }

    /**
     * Used to receive location updates via the {@link LocationProvider}
     */
    private final LocationProvider.LocationListener ownLocationListener = new LocationProvider.LocationListener() {
        @Override
        public void onLocation(LocationResult location) {
            Location loc = location.getLastLocation();
            if (loc == null)
                ownLocation.postValue(null);
            else
                ownLocation.postValue(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        @Override
        public void onLocationUnavailable() {
            ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.no_location_available, Snackbar.LENGTH_SHORT));
        }
    };

    /**
     * Given a list of contacts fetches and prepares markers for all exposures of the given contacts.
     * Only displays markers that have an actual location associated with them.
     * @param contactList
     */
    public void DisplayMarkersForContacts(List<Contact> contactList) {
        Log.d(TAG, "Got new contact list: " + contactList);
        selectedContacts.postValue(contactList);

        if (contactList == null) {
            markers.postValue(null);
        } else {
            isLoading.postValue(true);
            contactRepository.getContactsWithExposures(contactList, result -> {
                if (result instanceof Result.Success) {
                    executor.execute(() -> processExposures(((Result.Success<List<ContactDao.ContactWithExposures>>) result).data));
                } else {
                    // isLoading.postValue(false);
                    Log.e(TAG, "Failed to fetch contacts with exposures.", ((Result.Error<?>)result).exception);
                }
            });
        }
    }

    /**
     * Updates the topbottom sheet with RKI data for the currently selected location
     * @param forLocation The location to fetch
     * @apiNote Obviously only works inside of germany. Returns no information otherwise.
     */
    public void updateLocationInfo(LatLng forLocation) {
        Call<RKIApiResult> incidenceByLocation = api.getIncidenceByLocation(String.format(Locale.US,"%f,%f,", forLocation.longitude, forLocation.latitude));
        isLoading.postValue(true);
        incidenceByLocation.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RKIApiResult> call, @NonNull Response<RKIApiResult> response) {
                isLoading.postValue(false);

                if (response.body() == null || response.body().getFeatures().isEmpty()) {
                    locationInfos.postValue(null);
                    return;
                }

                Attributes attr = response.body().getFeatures().get(0).getAttributes();
                LocationInfo info = new LocationInfo(forLocation, attr.getBl(), attr.getCounty(), attr.getCases7Per100k());
                locationInfos.postValue(info);
            }

            @Override
            public void onFailure(@NonNull Call<RKIApiResult> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                locationInfos.postValue(null);
                Log.e(TAG, "Failed to fetch incidence by location", t);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.incidence_api_unavailable, Snackbar.LENGTH_SHORT));
            }
        });
    }

    /**
     * Requests to translate to the given latlang.
     * @param location
     */
    public void gotoLocation(LatLng location) {
        requestPosition.postValue(location);
    }

    /**
     * Processes the received list and posts a new list of MarkerOptions once done.
     * Should always be called on a background thread using the injected executor.
     * @param contactsWithExposures the retrieved list
     */
    private void processExposures(List<ContactDao.ContactWithExposures> contactsWithExposures) {
        Log.d(TAG, "Processing CWE...");

        List<MarkerOptions> newOptions = new ArrayList<>();

        float hue = 0;
        BitmapDescriptor descriptor;

        // By calculating the center of gravity we can go to a position on the map
        // where the most exposures are likely to be.
        long exposureCount = 0;
        double cogLng = 0;
        double cogLat = 0;

        for (ContactDao.ContactWithExposures cwe : ListUtils.emptyIfNull(contactsWithExposures)) {
            descriptor = BitmapDescriptorFactory.defaultMarker(hue);
            hue = (hue + 11) % 360;

            for (Exposure exposure : ListUtils.emptyIfNull(cwe.exposures)) {
                if (exposure.location == null) continue;

                MarkerOptions options = new MarkerOptions()
                        .draggable(false)
                        .position(exposure.location)
                        .title(cwe.contact.displayName + " - " + exposure.startDate)
                        .icon(descriptor);

                newOptions.add(options);

                // For calculating the "center of gravity"
                exposureCount++;
                cogLng += exposure.location.longitude;
                cogLat += exposure.location.latitude;
            }
        }

        if (exposureCount > 0) {
            requestPosition.postValue(new LatLng(cogLat / exposureCount, cogLng / exposureCount));
        }

        isLoading.postValue(false);
        markers.postValue(newOptions);
    }

    /**
     * Location information as returned by the RKI api to be provided to the map view.
     */
    public static class LocationInfo {
        public LatLng location;
        public String county;
        public String state;
        public double incidence;

        public LocationInfo(LatLng location, String state, String county, double incidence) {
            this.location = location;
            this.state = state;
            this.county = county;
            this.incidence = incidence;
        }
    }
}
