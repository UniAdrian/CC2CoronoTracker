package de.uni.cc2coronotracker.data.repositories.providers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.LinkedList;
import java.util.List;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

public class LocationProvider implements LifecycleObserver {
    public interface LocationListener {
        void onLocation(LocationResult location);
        void onLocationUnavailable();
    }

    private final String TAG = "LocationProvider";

    private static final long UPDATE_INTERVAL = 10000; // 10 secs
    private static final long FASTEST_INTERVAL = 2000; // 2 secs

    private final ContextMediator ctxMediator;
    private final Context context;

    private final FusedLocationProviderClient fusedLocationClient;

    private final LocationRequest locationRequest;
    private final LocationCallback locationCallback;

    private final MutableLiveData<LocationResult> locationResults = new MutableLiveData<>();

    private final List<LocationListener> locationListeners = new LinkedList<>();

    public void addLocationListener(LocationListener listener) {
        if (locationListeners.size() == 0) {
            startTracking();
        }
        locationListeners.add(listener);
    }


    public void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);

        if (locationListeners.size() == 0) {
            stopTracking();
        }
    }

    public LocationProvider(@ApplicationContext Context applicationContext, ContextMediator ctxMediator) {
        context = applicationContext;
        this.ctxMediator = ctxMediator;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Notify all listeners, this allows our viewmodels to use this as well.
                for (LocationListener listener : locationListeners) {
                    listener.onLocation(locationResult);
                }
                locationResults.postValue(locationResult);
            }
        };

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }

    private void startTracking() {
        Log.d(TAG, "waiting for permission...");
        ctxMediator.request(RequestFactory.createPermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                checkSettingsAndProceed();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                if (permissionDeniedResponse.isPermanentlyDenied()) {
                    Log.e(TAG, "Permission permanently denied.");
                }
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.no_permission_no_location, Snackbar.LENGTH_LONG));
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }));
    }

    /**
     * Checks the state of the location service and reacts accordingly
     * e.g. proceeding to listen on success or prompting the user to change the settings otherwise.
     */
    @SuppressLint("MissingPermission")
    private void checkSettingsAndProceed() {
        ctxMediator.request(RequestFactory.createActivateLocationRequest(locationRequest, result -> {
            if (result instanceof Result.Success) {
                Log.d(TAG, "Starting to track location...");
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } else {
                Log.e(TAG, "Location unavailable...", ((Result.Error)result).exception);
                notifyAndStop();
            }
        }));
    }

    @SuppressLint("MissingPermission")
    public void onSettingsResult(int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            Log.d(TAG, "Starting to track location...");
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Log.e(TAG, "Location unavailable... Code: " + resultCode);
            notifyAndStop();
        }
    }

    private void notifyAndStop() {
        for (LocationListener listener : locationListeners) {
            listener.onLocationUnavailable();
        }
        stopTracking();
    }

    private void stopTracking() {
        Log.d(TAG, "Stopping to track location...");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    /**
     * Tries to fetch the last known location of the device.
     * May request permission from the user before returning.
     *
     * Note: The lint suppression is required here since it does not recognize dexter.
     * @param callback Called once results are available.
     */
    @SuppressLint("MissingPermission")
    public void getLastLocation(RepositoryCallback<Location> callback) {
        ctxMediator.request(RequestFactory.createPermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            callback.onComplete(new Result.Success<>(location));
                            return;
                        }

                        callback.onComplete(new Result.Error<>(new RuntimeException("Service unavailable")));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error trying to get last location", e);
                        callback.onComplete(new Result.Error<>(e));
                    });
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                if (permissionDeniedResponse.isPermanentlyDenied()) {
                    Log.e(TAG, "Permission permanently denied.");
                }
                callback.onComplete(new Result.Error<>(new SecurityException("Missing permission")));
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }));
    }
}
