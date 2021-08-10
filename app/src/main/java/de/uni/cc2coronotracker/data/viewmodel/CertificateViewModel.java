package de.uni.cc2coronotracker.data.viewmodel;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.qr.EGC;
import de.uni.cc2coronotracker.data.repositories.CertificateRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;

@HiltViewModel
public class CertificateViewModel extends ViewModel {

    private static final String TAG = "CertificateVM";

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> imgQR = new MutableLiveData<>();

    private EGC currentCertificate = null;
    private final CertificateRepository repository;

    @Inject
    public CertificateViewModel(CertificateRepository repository) {
        this.repository = repository;
    }

    public void setCurrentCertificate(EGC egc) {
        this.currentCertificate = egc;
        loading.postValue(true);

        repository.certToQr(300, egc.raw, result -> {
            if (result instanceof Result.Success) {
                imgQR.postValue(((Result.Success<Bitmap>) result).data);
            } else {
                Log.e(TAG, "", ((Result.Error<Bitmap>)result).exception);
                imgQR.postValue(null);
            }
            loading.postValue(false);
        });
    }

    public LiveData<Bitmap> getQr() {return imgQR; }
    public LiveData<Boolean> isLoading() {
        return loading;
    }
}