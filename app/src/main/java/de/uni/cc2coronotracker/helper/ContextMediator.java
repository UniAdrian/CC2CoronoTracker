package de.uni.cc2coronotracker.helper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * When injected into a viewmodel allows the viewmodel to request certain actions from the activity without
 * itself referencing context.
 */
public class ContextMediator {

    // Allows arbitrary calls with context
    private final MutableLiveData<Event<CallWithContextRequest>> requests = new MutableLiveData<>();

    public ContextMediator() {
    }

    public LiveData<Event<CallWithContextRequest>> getRequests() {
        return requests;
    }

    public void request(CallWithContextRequest request) {
        requests.postValue(new Event<>(request));
    }
}
