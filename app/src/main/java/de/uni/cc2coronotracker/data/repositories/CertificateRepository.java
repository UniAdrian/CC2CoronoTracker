package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.dao.CertificateDao;
import de.uni.cc2coronotracker.data.models.CertEntity;
import de.uni.cc2coronotracker.data.qr.EGC;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class CertificateRepository {

    private final Context appContext;
    private final Executor executor;
    private final CertificateDao dao;

    @Inject
    public CertificateRepository(@ApplicationContext Context appContext, Executor executor, CertificateDao dao) {
        this.appContext = appContext;
        this.executor = executor;
        this.dao = dao;
    }

    /**
     * Stores the given egc in the database
     * @param egc The egc to be stored
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void addEGC(EGC egc, RepositoryCallback<Long> callback) {
        executor.execute( () -> {
            try {
                callback.onComplete(new Result.Success<>(dao.insertCert(CertEntity.fromEGC(egc))));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Returns a certificate by its unique identifier
     * @param identifier The identifier of the certificate
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void getByIdentifier(String identifier, RepositoryCallback<CertEntity> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(dao.getCert(identifier)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * @return All certificates as LiveData
     */
    public LiveData<List<CertEntity>> getAll() {
        return dao.getAll();
    }

    /**
     * Given a raw string generates and returns a new bitmap containing the QR Code
     * @param dimension The dimensions of the square QR code
     * @param raw The raw string to be put into the QR code
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void certToQr(int dimension, String raw, RepositoryCallback<Bitmap> callback) {
        executor.execute(() -> {

            QRGEncoder qrgEncoder = new QRGEncoder(raw, null, QRGContents.Type.TEXT, dimension + 50);

            int qrBlack = ResourcesCompat.getColor(appContext.getResources(), R.color.qr_black, null);
            int qrWhite =  ResourcesCompat.getColor(appContext.getResources(), R.color.qr_white, null);
            qrgEncoder.setColorBlack(qrBlack);
            qrgEncoder.setColorWhite(qrWhite);

            try {
                // Getting QR-Code as Bitmap
                // Using a workaround around a (currently buggy?) library restriction that adds a very large margin
                Bitmap bitmap = Bitmap.createBitmap(qrgEncoder.getBitmap(), 25, 25, dimension, dimension);
                callback.onComplete(new Result.Success<>(bitmap));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
