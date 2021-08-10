package de.uni.cc2coronotracker.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.qr.EGC;
import de.uni.cc2coronotracker.data.repositories.CertificateRepository;

@HiltViewModel
public class CertificateViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private EGC currentCertificate = null;
    private final CertificateRepository repository;

    @Inject
    public CertificateViewModel(CertificateRepository repository) {
        this.repository = repository;
    }

    public void setCurrentCertificate(EGC egc) {
        this.currentCertificate = egc;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }
}