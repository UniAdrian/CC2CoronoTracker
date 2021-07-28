package de.uni.cc2coronotracker.data.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class MapsViewModel extends ViewModel {

    private final String TAG = "MapsViewModel";

    private MutableLiveData<LatLng> requestPosition = new MutableLiveData<>();
    private MutableLiveData<List<MarkerOptions>> markers = new MutableLiveData<>();

    private final LocationProvider locationProvider;
    private final ContextMediator ctxMediator;

    @Inject
    public MapsViewModel(ContextMediator mediator, LocationProvider locationProvider) {
        ctxMediator = mediator;
        this.locationProvider = locationProvider;
    }


    public LiveData<LatLng> getGotoPosition() {
        return requestPosition;
    }

    public LiveData<List<MarkerOptions>> getMarkers() {
        return markers;
    }

    public void onMapReady() {
        locationProvider.getLastLocation(result -> {
            if (result instanceof Result.Success) {
                Location loc = ((Result.Success<Location>) result).data;
                requestPosition.postValue(new LatLng(loc.getLatitude(), loc.getLongitude()));
            } else {
                Log.e(TAG, "Failed to fetch location data", ((Result.Error)result).exception);
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.location_not_available, Snackbar.LENGTH_LONG));
            }
        });
    }

    public void selectContacts(List<Contact> contactList) {
        if (contactList == null) {
            // TODO: Fetch all exposures, brush em up and display them.
        }
    }
}
