package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborParseException;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import COSE.CoseException;
import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.CertEntity;
import de.uni.cc2coronotracker.data.qr.EGC;
import de.uni.cc2coronotracker.data.repositories.CertificateRepository;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.EGCHelper;
import de.uni.cc2coronotracker.helper.RequestFactory;
import de.uni.cc2coronotracker.ui.views.CertificateListFragmentDirections;

@HiltViewModel
public class CertificateListViewModel extends ViewModel {

    private final static String TAG = "CertificateListVM";

    private final ContextMediator ctxMediator;

    private final LiveData<List<CertEntity>> allCerts;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    @Inject
    public CertificateListViewModel(CertificateRepository certificateRepository, ContextMediator ctxMediator) {
        this.ctxMediator = ctxMediator;

        allCerts = Transformations.map(certificateRepository.getAll(), input -> {
            isLoading.postValue(false);
            return input;
        });
    }


    public void gotoQRScan() {
        ctxMediator.request(RequestFactory.createNavigationRequest(R.id.action_certificateListFragment_to_readQR));
    }

    public void gotoDetails(CertEntity entity) {
        try {
            EGC egc = EGCHelper.parse(entity.raw);
            CertificateListFragmentDirections.ActionCertificateListFragmentToCertificateFragment actionCertificateListFragmentToCertificateFragment = CertificateListFragmentDirections.actionCertificateListFragmentToCertificateFragment(egc);
            ctxMediator.request(RequestFactory.createNavigationRequest(actionCertificateListFragmentToCertificateFragment));
        } catch (CborConversionException | CborParseException | IOException | DataFormatException | CoseException e) {
            Log.e(TAG, "Failed to parse raw cert. This should not happen, since we only store certs that ARE valid.", e);
        }
    }

    public LiveData<Boolean> getLoading() {
        return isLoading;
    }

    public LiveData<List<CertEntity>> getCerts() {
        return allCerts;
    }
}