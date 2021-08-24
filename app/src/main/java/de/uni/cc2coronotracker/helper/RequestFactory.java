package de.uni.cc2coronotracker.helper;

import android.content.DialogInterface;
import android.content.IntentSender;
import android.util.Log;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.concurrent.CancellationException;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.ui.dialogs.NewContactDialogFragment;
import de.uni.cc2coronotracker.ui.dialogs.SelectContactDialogFragment;
import de.uni.cc2coronotracker.ui.views.MainActivity;

/**
 * Provides easy access to commonly used requests.
 * Used in conjunction with the {@link ContextMediator}
 */
public class RequestFactory {

    public static CallWithContextRequest createPermissionRequest(String permission, PermissionListener resultListener) {
        CallWithContextRequest.ContextfulCall call = c -> Dexter.withContext(c)
                .withPermission(permission)
                .withListener(resultListener).check();

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createSnackbarRequest(int messageResId, int duration, Object... messageArgs) {
        CallWithContextRequest.ContextfulCall call = c -> {
            String message = c.getResources().getString(messageResId, messageArgs);
            Snackbar.make(((AppCompatActivity) c).findViewById(android.R.id.content),
                    message,
                    duration)
                    .show();
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createSnackbarRequest(int messageResId, int duration, int actionResId, View.OnClickListener actionMethod, Object... messageArgs) {
        CallWithContextRequest.ContextfulCall call = c -> {
            String message = c.getResources().getString(messageResId, messageArgs);
            Snackbar.make(((AppCompatActivity) c).findViewById(android.R.id.content),
                    message,
                    duration)
                    .setAction(actionResId, actionMethod)
                    .show();
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createConfirmationDialogRequest(int messageResId, int titleResId, @Nullable android.content.DialogInterface.OnClickListener yesAction, @Nullable DialogInterface.OnClickListener noAction, Object... messageArgs) {
        CallWithContextRequest.ContextfulCall call = c -> {
            String message = c.getResources().getString(messageResId, messageArgs);
            new AlertDialog.Builder(c)
                    .setTitle(titleResId)
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, yesAction)
                    .setNegativeButton(android.R.string.cancel, noAction).show();
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createNavigationRequest(@NonNull NavDirections where) {
        CallWithContextRequest.ContextfulCall call = c -> Navigation.findNavController((AppCompatActivity) c, R.id.nav_host_fragment).navigate(where);

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createNavigationRequest(@IdRes int where) {
        CallWithContextRequest.ContextfulCall call = c -> Navigation.findNavController((AppCompatActivity) c, R.id.nav_host_fragment).navigate(where);

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createContactSelectionDialogRequest(boolean isMultiSelect, @Nullable QrIntent.Intent callerIntent) {
        CallWithContextRequest.ContextfulCall call = c -> SelectContactDialogFragment.newInstance(isMultiSelect, callerIntent).show(((AppCompatActivity)c).getSupportFragmentManager(), "");

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createNewContactDialogRequest() {
        return createNewContactDialogRequest(null);
    }

    public static CallWithContextRequest createNewContactDialogRequest(@Nullable Contact toEdit) {
        CallWithContextRequest.ContextfulCall call = c -> NewContactDialogFragment.newInstance(toEdit).show(((AppCompatActivity)c).getSupportFragmentManager(), "");

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createActivateLocationRequest(LocationRequest request, @NonNull RepositoryCallback<?> callback) {
        CallWithContextRequest.ContextfulCall call = c -> {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

            SettingsClient settingsClient = LocationServices.getSettingsClient(c);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
            task.addOnSuccessListener(locationSettingsResponse -> callback.onComplete(new Result.Success<>(null)));

            task.addOnFailureListener(((AppCompatActivity)c), e -> {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult((AppCompatActivity)c, MainActivity.LOCATION_AVAILABILITY_REQUEST);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e("ActivateLocationRequest", "Failed to prompt settings change", sendEx);
                    }
                } else {
                    Log.e("ActivateLocationRequest", "Failed to prompt settings change", e);
                    callback.onComplete(new Result.Error<>(new UnsupportedOperationException("The location service is unavailable.")));
                }
            });

            task.addOnCanceledListener(() -> callback.onComplete(new Result.Error<>(new CancellationException("The user did not activate the location service."))));
        };


        return new CallWithContextRequest(call);
    }

}
