package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

/**
 * Stores the personal UUID, settings etc. required by this app
 */
public class AppRepository {

    private Context applicationContext;
    private final Executor executor;


    @Inject
    public AppRepository(@ApplicationContext Context ctx, Executor executor) {
        applicationContext = ctx;
        this.executor = executor;
    }

    public void generateQRCode(String inputValue, int dimension, RepositoryCallback<Bitmap> callback) {
        executor.execute(() -> {
            QRGEncoder qrgEncoder = new QRGEncoder(inputValue, null, QRGContents.Type.TEXT, dimension);

            qrgEncoder.setColorBlack(R.color.qr_black);
            qrgEncoder.setColorWhite(R.color.qr_white);

            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);

            try {
                // Getting QR-Code as Bitmap
                Bitmap bitmap = qrgEncoder.getBitmap();
                callback.onComplete(new Result.Success<>(bitmap));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
