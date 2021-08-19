package de.uni.cc2coronotracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.LifecycleService;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.ui.views.OngoingExposureActivity;

/**
 * Allows the user to close the app and still have an ongoing exposure that he can interact with
 * via a foreground service.
 */
public class OngoingExposureService extends LifecycleService {

    public static final String CHANNEL_ID = "OngoingExposureSC";
    public static final String STOP_SERVICE = "STOP_SERVICE";

    /**
     * Called when the service is created, starts or stops the service.
     * @param intent The intent received
     * @param flags The flags received
     * @param startId The start id received
     * @return START_NOT_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(STOP_SERVICE)) {
            // Stop the service.
            stopForeground(true);
            stopSelfResult(startId);
        } else {
            // Setup foreground and notification
            long exposure_id = intent.getLongExtra("exposure_id", -1);
            long exposure_start = intent.getLongExtra("exposure_start", -1);

            createNotificationChannel();
            setupForeground(exposure_id, exposure_start);
        }

        return START_NOT_STICKY;
    }

    /**
     * Sets up for usage as a foreground service
     * @param exposure_id
     * @param exposure_start
     */
    private void setupForeground(long exposure_id, long exposure_start) {
        // Create the bundle of metadata required
        Bundle argBundle = new Bundle();
        argBundle.putLong("exposure_id", exposure_id);
        argBundle.putBoolean("exposure_flag_end", true);

        // Prepare the intent for content click
        Intent notificationIntent = new Intent(getApplicationContext(), OngoingExposureActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                .putExtras(argBundle);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(notificationIntent);

        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(42069, PendingIntent.FLAG_UPDATE_CURRENT);


        // Fetch the required strings from the resources
        Resources res = getApplicationContext().getResources();

        Date date=new Date(exposure_start);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateText = df2.format(date);

        String shortTitle = res.getString(R.string.ongoing_exposure_svc_title_short);
        String longTitle = res.getString(R.string.ongoing_exposure_svc_title_long);
        String shortDesc = res.getString(R.string.ongoing_exposure_svc_desc_short);
        String longDesc = res.getString(R.string.ongoing_exposure_svc_desc_long, dateText);


        // Setup the notification itself
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(longTitle);
        bigTextStyle.bigText(longDesc);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(shortTitle)
                .setContentText(shortDesc)
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_meeting_24)
                .setContentIntent(pendingIntent)
                .setUsesChronometer(true)
                .build();

        // last but not least start the service as a foreground service.
        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        // We do not support binding.
        return null;
    }

    /**
     * Compatibility: Create a notification channel if needed.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Ongoing Exposure Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
