package de.uni.cc2coronotracker.data.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.collections4.ListUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.helper.ContextMediator;

@HiltViewModel
public class MapsViewModel extends ViewModel {

    private final String TAG = "MapsViewModel";

    private MutableLiveData<LatLng> requestPosition = new MutableLiveData<>();
    private MutableLiveData<List<MarkerOptions>> markers = new MutableLiveData<>();

    private MutableLiveData<List<Contact>> selectedContacts = new MutableLiveData<>();

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final LocationProvider locationProvider;
    private final ContactRepository contactRepository;

    private final Executor executor;
    private final ContextMediator ctxMediator;

    @Inject
    public MapsViewModel(ContextMediator mediator, LocationProvider locationProvider, ContactRepository contactRepository, Executor executor) {
        ctxMediator = mediator;
        this.executor = executor;
        this.locationProvider = locationProvider;
        this.contactRepository = contactRepository;
    }


    public LiveData<List<Contact>> getSelectedContacts() {return selectedContacts; }
    public LiveData<LatLng> getGotoPosition() {
        return requestPosition;
    }
    public LiveData<List<MarkerOptions>> getMarkers() {
        return markers;
    }
    public LiveData<Boolean> getIsLoading() {return isLoading; }

    public void onMapReady() {
        locationProvider.getLastLocation(result -> {
            if (result instanceof Result.Success) {
                Location loc = ((Result.Success<Location>) result).data;
                requestPosition.postValue(new LatLng(loc.getLatitude(), loc.getLongitude()));
            } else {
                Log.e(TAG, "Failed to fetch location data", ((Result.Error)result).exception);
                // ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.location_not_available, Snackbar.LENGTH_LONG));
            }
        });
    }

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
                    Log.e(TAG, "Failed to fetch contacts with exposures.", ((Result.Error)result).exception);
                }
            });
        }
    }

    /**
     * Processes the received list and posts a new list of MarkerOptions once done.
     * Should always be called on a background thread using the injected executor.
     * @param contactsWithExposures
     */
    private void processExposures(List<ContactDao.ContactWithExposures> contactsWithExposures) {
        Log.d(TAG, "Processing CWE...");


        // TODO: REMOVE ME. I AM DEBUG CODE
        for (int i=0; i<10; ++i) {
            ContactDao.ContactWithExposures cwe = new ContactDao.ContactWithExposures();
            cwe.contact = new Contact();
            cwe.contact.displayName = "Test Account #" + i;
            cwe.contact.id = 0;

            Random rng = new Random();

            int n = 0 + (int)(Math.random() * 30);
            cwe.exposures = new ArrayList<>(n);
            for (int j=0; j<n; ++j) {
                double rndLat = rng.nextDouble() * .05 - .025;
                double rndLng = rng.nextDouble() * .05 - .025;
                LatLng kassel = new LatLng(51.312801 + rndLat, 9.481544 + rndLng);

                Exposure exp = new Exposure();
                exp.location = kassel;
                exp.date = new Date(new java.util.Date().getTime());

                cwe.exposures.add(exp);
            }

            contactsWithExposures.add(cwe);
        }

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
                        .title(cwe.contact.displayName + " - " + exposure.date)
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
}
