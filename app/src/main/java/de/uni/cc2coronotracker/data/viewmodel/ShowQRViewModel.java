package de.uni.cc2coronotracker.data.viewmodel;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.repositories.AppRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class ShowQRViewModel extends ViewModel {

    private final String TAG = "ShowQRViewModel";

    private final ContextMediator contextMediator;
    private final AppRepository appRepository;

    private MutableLiveData<Bitmap> qrCode = new MutableLiveData<>();

    @Inject
    public ShowQRViewModel(ContextMediator ctxMediator, AppRepository appRepository) {
        contextMediator = ctxMediator;
        this.appRepository = appRepository;
    }

    /**
     * First tries to find a stored QR code, if none exists it creates a new one and - if the user
     * allows it - stores it on permanent storage.
     */
    public void createOrGetQRCode(String value, int dimension) {
        appRepository.generateQRCode(value, dimension, result -> {
            if (result instanceof Result.Success) {
                qrCode.postValue(((Result.Success<Bitmap>) result).data);
            } else {
                Log.e(TAG, "Failed to create or get QR code.", ((Result.Error)result).exception);
                contextMediator.request(RequestFactory.createSnackbarRequest(R.string.qr_generation_failed, Snackbar.LENGTH_LONG, R.string.retry, (v) -> {
                    createOrGetQRCode(value, dimension);
                }));
            }
        });
    }


    public LiveData<Bitmap> getQRCode() {
        return qrCode;
    }

}
