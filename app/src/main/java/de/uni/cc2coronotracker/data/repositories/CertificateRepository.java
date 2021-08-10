package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.data.models.CertEntity;
import de.uni.cc2coronotracker.data.dao.CertificateDao;
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

    public void addEGC(EGC egc, RepositoryCallback<Long> callback) {
        executor.execute( () -> {
            try {
                callback.onComplete(new Result.Success<>(dao.insertCert(CertEntity.fromEGC(egc))));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    public void getByIdentifier(String identifier, RepositoryCallback<CertEntity> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(dao.getCert(identifier)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    public LiveData<List<CertEntity>> getAll() {
        return dao.getAll();
    }
}
