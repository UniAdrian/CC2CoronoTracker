package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

/**
 * Stores the personal UUID, settings etc. required by this app
 */
public class AppRepository {

    private final String PREFERENCE_UUID = "app_uuid";


    private final Context applicationContext;
    private final Executor executor;

    private final SharedPreferences preferences;

    private final MutableLiveData<UUID> attachedUUID = new MutableLiveData<>();

    @Inject
    public AppRepository(@ApplicationContext Context ctx, Executor executor, SharedPreferences preferences) {
        applicationContext = ctx;
        this.executor = executor;
        this.preferences = preferences;

        getOrCreateUUID();
    }

    /**
     * Tries to fetch the app uuid from shared preferences
     * If no uuid was found a new one is created and stored.
     * @return The stored or newly created uuid
     */
    private UUID getOrCreateUUID() {
        String uuidAsString = preferences.getString(PREFERENCE_UUID, null);

        if (uuidAsString!=null) {
            UUID uuid = UUID.fromString(uuidAsString);
            attachedUUID.postValue(uuid);
            return uuid;
        }

        // We need to create a new UUID.
        UUID newUUID = UUID.randomUUID();
        // Store the new uuid. Since the edit is a potentially expensive operation we push this to the background.
        preferences.edit().putString(PREFERENCE_UUID, newUUID.toString()).commit();
        attachedUUID.postValue(newUUID);
        return newUUID;
    }

    public void getOrCreateUUID(RepositoryCallback<UUID> callback) {
        executor.execute(() -> {
            try {
                UUID uuid = getOrCreateUUID();
                callback.onComplete(new Result.Success<>(uuid));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    public void generateExposureQRCode(int dimension, RepositoryCallback<Bitmap> callback) {
        executor.execute(() -> {
            QrIntent.AddExposure intent = new QrIntent.AddExposure(getOrCreateUUID(), true);
            QRGEncoder qrgEncoder = new QRGEncoder(intent.toString(), null, QRGContents.Type.TEXT, dimension);

            int qrBlack = ResourcesCompat.getColor(applicationContext.getResources(), R.color.qr_black, null);
            int qrWhite =  ResourcesCompat.getColor(applicationContext.getResources(), R.color.qr_white, null);
            qrgEncoder.setColorBlack(qrBlack);
            qrgEncoder.setColorWhite(qrWhite);

            try {
                // Getting QR-Code as Bitmap
                Bitmap bitmap = qrgEncoder.getBitmap();
                callback.onComplete(new Result.Success<>(bitmap));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }


    public LiveData<UUID> getAttachedUUID() {
        return attachedUUID;
    }
}
