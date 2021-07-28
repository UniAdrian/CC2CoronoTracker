package de.uni.cc2coronotracker.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.PermissionListener;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.ui.dialogs.NewContactDialogFragment;
import de.uni.cc2coronotracker.ui.dialogs.SelectContactDialogFragment;

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
            Snackbar.make(((Activity) c).findViewById(android.R.id.content),
                    message,
                    duration)
                    .show();
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createSnackbarRequest(int messageResId, int duration, int actionResId, View.OnClickListener actionMethod, Object... messageArgs) {
        CallWithContextRequest.ContextfulCall call = c -> {
            String message = c.getResources().getString(messageResId, messageArgs);
            Snackbar.make(((Activity) c).findViewById(android.R.id.content),
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
                    .setPositiveButton(android.R.string.yes, yesAction)
                    .setNegativeButton(android.R.string.no, noAction).show();
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createNavigationRequest(@NonNull NavDirections where) {
        CallWithContextRequest.ContextfulCall call = c -> {
            Navigation.findNavController((Activity) c, R.id.nav_host_fragment).navigate(where);
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createNavigationRequest(@IdRes int where) {
        CallWithContextRequest.ContextfulCall call = c -> {
            Navigation.findNavController((Activity) c, R.id.nav_host_fragment).navigate(where);
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createContactSelectionDialogRequest(boolean isMultiSelect, @Nullable QrIntent.Intent callerIntent) {
        CallWithContextRequest.ContextfulCall call = c -> {
            SelectContactDialogFragment.newInstance(isMultiSelect, callerIntent).show(((AppCompatActivity)c).getSupportFragmentManager(), "");
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createNewContactDialogRequest() {
        CallWithContextRequest.ContextfulCall call = c -> {
            NewContactDialogFragment.newInstance().show(((AppCompatActivity)c).getSupportFragmentManager(), "");
        };

        return new CallWithContextRequest(call);
    }

    public static CallWithContextRequest createOpenLocationSettingsRequest() {
        CallWithContextRequest.ContextfulCall call = c -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(c, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
            alertDialog.setTitle(R.string.enable_location);
            alertDialog.setMessage(R.string.enable_location_reason);
            alertDialog.setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    c.startActivity(intent);
                }
            });

            alertDialog.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.cancel());
            alertDialog.show();
        };

        return new CallWithContextRequest(call);
    }

}
