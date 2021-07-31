package de.uni.cc2coronotracker.data.viewmodel;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.repositories.AppRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class PreferencesViewModel extends ViewModel {

    private final String TAG = "PreferencesViewModel";

    private final ContextMediator contextMediator;
    private final AppRepository appRepository;

    private MutableLiveData<Bitmap> qrCode = new MutableLiveData<>();
    private LiveData<UUID> uuidLD;

    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    @Inject
    public PreferencesViewModel(ContextMediator ctxMediator, AppRepository appRepository) {
        contextMediator = ctxMediator;
        this.appRepository = appRepository;

        uuidLD = appRepository.getAttachedUUID();
    }

    /**
     * First tries to find a stored QR code, if none exists it creates a new one and - if the user
     * allows it - stores it on permanent storage.
     */
    public void createOrGetPersonalQRCode(int dimension) {
        loading.postValue(true);
        appRepository.generateExposureQRCode(dimension, result -> {
            loading.postValue(false);
            if (result instanceof Result.Success) {
                qrCode.postValue(((Result.Success<Bitmap>) result).data);
            } else {
                Log.e(TAG, "Failed to create or get QR code.", ((Result.Error)result).exception);
                contextMediator.request(RequestFactory.createSnackbarRequest(R.string.qr_generation_failed, Snackbar.LENGTH_LONG, R.string.retry, (v) -> {
                    createOrGetPersonalQRCode(dimension);
                }));
            }
        });
    }


    public LiveData<Bitmap> getQRCode() { return qrCode; }
    public LiveData<UUID> getUUID() { return uuidLD; }
    public LiveData<Boolean> getLoading() {return loading; }
}
