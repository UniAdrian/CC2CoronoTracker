package de.uni.cc2coronotracker.helper;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Requests to call the given run method with a valid context.
 */
public class CallWithContextRequest {

    public interface ContextfulCall {
        void run(Context c);
    }

    private final ContextfulCall toCall;

    public CallWithContextRequest(@NonNull ContextfulCall toCall) {
        this.toCall = toCall;
    }

    public void run(Context c) {
        this.toCall.run(c);
    }
    public ContextfulCall getCall() {
        return toCall;
    }
}
