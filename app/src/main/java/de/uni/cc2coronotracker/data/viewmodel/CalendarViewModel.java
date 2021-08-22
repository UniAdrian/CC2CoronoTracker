package de.uni.cc2coronotracker.data.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final String TAG = "CalendarVM";
    private final ContextMediator ctxMediator;
    private final ExposureRepository exposureRepository;
    private final ContactRepository contactRepository;
    private final List<Contact> contactList = new ArrayList<>();

    private List<Exposure> _exposures = new ArrayList<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public class ExposureDisplayInfo {
        public final Exposure exposureData;
        public final String contactName;
        public final Uri contactPhotoUri;
        public ExposureDisplayInfo(Exposure exposure, String contactName, Uri contactPhotoUri) {
            this.exposureData = exposure;
            this.contactName = contactName;
            this.contactPhotoUri = contactPhotoUri;
        }
    }

    private MutableLiveData<List<ExposureDisplayInfo>> exposureInfo = new MutableLiveData<>();
    public MutableLiveData<List<ExposureDisplayInfo>> getExposureInfo() {
        return exposureInfo;
    }
    public ExposureDisplayInfo getExposureInfoById (long id) {
        List<ExposureDisplayInfo> infoList = exposureInfo.getValue();
        for (ExposureDisplayInfo info: infoList) {
            if (info.exposureData.id == id)
                return info;
        }
        return null;
    }
    private MutableLiveData<Boolean> getIsLoading() { return isLoading; }

    @Inject
    public CalendarViewModel(ContextMediator ctxMediator,
                             @NonNull ExposureRepository exposureRepository,
                             @NonNull ContactRepository contactRepository) {
        this.ctxMediator = ctxMediator;
        this.exposureRepository = exposureRepository;
        this.contactRepository = contactRepository;
    }

    public void fetchExposure(Date date) {
        exposureInfo.postValue(new ArrayList<>());
        exposureRepository.getExposuresByDate(date, queryResult -> {
            isLoading.postValue(true);
            if (queryResult instanceof Result.Success) {
                _exposures = ((Result.Success<List<Exposure>>) queryResult).data;
                List<ExposureDisplayInfo> e = new ArrayList<>();
                if (_exposures.size() != 0) {
                    for (Exposure exposure : _exposures) {
                        Contact contact = contactRepository.getContactSync(exposure.contactId);
                        e.add(new ExposureDisplayInfo(exposure, contact.displayName, contact.photoUri));
                    }
                    exposureInfo.postValue(e);
                }
                isLoading.postValue(false);
            } else {
                _exposures = new ArrayList<>();
                Exception e = ((Result.Error<?>) queryResult).exception;
                Log.e(TAG, "Failed to fetch exposures.", e);
                exposureInfo.postValue(new ArrayList<>());
            }
        });
    }

}

