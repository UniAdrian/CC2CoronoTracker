package de.uni.cc2coronotracker.helper;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.PermissionListener;

public class RequestFactory {

    public static CallWithContextRequest createPermissionRequest(String permission, PermissionListener resultListener) {
        CallWithContextRequest.ContextfulCall call = new CallWithContextRequest.ContextfulCall() {
            @Override
            public void run(Context c) {
                Dexter.withContext(c)
                        .withPermission(permission)
                        .withListener(resultListener).check();
            }
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createSnackbarRequest(int messageResId, int duration) {
        CallWithContextRequest.ContextfulCall call = new CallWithContextRequest.ContextfulCall() {
            @Override
            public void run(Context c) {
                Snackbar.make(((Activity)c).findViewById(android.R.id.content),
                        messageResId,
                        duration)
                        .show();
            }
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createSnackbarRequest(int messageResId, int duration, int actionResId, View.OnClickListener actionMethod) {
        CallWithContextRequest.ContextfulCall call = new CallWithContextRequest.ContextfulCall() {
            @Override
            public void run(Context c) {
                Snackbar.make(((Activity)c).findViewById(android.R.id.content),
                        messageResId,
                        duration)
                        .setAction(actionResId, actionMethod)
                        .show();
            }
        };

        return new CallWithContextRequest(call);
    }

}
