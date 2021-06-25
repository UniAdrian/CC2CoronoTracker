package de.uni.cc2coronotracker.data.viewmodel.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import de.uni.cc2coronotracker.data.models.Exposure;

/**
 * Used to share data beteween the read QR and the contact fragments.
 */
public class AddExposureSharedViewModel extends ViewModel {
    MutableLiveData<Exposure> uuidsToAdd = new MutableLiveData<>();

    public LiveData<Exposure> getExposuresToAdd() {
        return uuidsToAdd;
    }
    public void addUuid(Exposure toAdd) {
        uuidsToAdd.postValue(toAdd);
    }
}
