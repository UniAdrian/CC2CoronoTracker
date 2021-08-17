package de.uni.cc2coronotracker.helper;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

/**
 * When injected into a viewmodel allows the viewmodel to request certain actions from the activity without
 * itself referencing context.
 */
public class ContextMediator {

    // Allows arbitrary calls with context
    private final MutableLiveData<Event<CallWithContextRequest>> requests = new MutableLiveData<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public ContextMediator() {
    }

    public LiveData<Event<CallWithContextRequest>> getRequests() {
        return requests;
    }

    public void request(CallWithContextRequest request) {
        mainHandler.post(() -> requests.setValue(new Event<>(request)));
    }
}
