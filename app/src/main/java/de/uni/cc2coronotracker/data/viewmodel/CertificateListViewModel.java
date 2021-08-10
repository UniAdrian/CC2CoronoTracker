package de.uni.cc2coronotracker.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.CertEntity;
import de.uni.cc2coronotracker.data.repositories.CertificateRepository;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class CertificateListViewModel extends ViewModel {

    private final CertificateRepository certificateRepository;
    private final ContextMediator ctxMediator;

    private final LiveData<List<CertEntity>> allCerts;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    @Inject
    public CertificateListViewModel(CertificateRepository certificateRepository, ContextMediator ctxMediator) {
        this.certificateRepository = certificateRepository;
        this.ctxMediator = ctxMediator;

        allCerts = Transformations.map(certificateRepository.getAll(), input -> {
            isLoading.postValue(false);
            return input;
        });
    }


    public void gotoQRScan() {
        ctxMediator.request(RequestFactory.createNavigationRequest(R.id.action_certificateListFragment_to_readQR));
    }

    public LiveData<Boolean> getLoading() {
        return isLoading;
    }

    public LiveData<List<CertEntity>> getCerts() {
        return allCerts;
    }
}