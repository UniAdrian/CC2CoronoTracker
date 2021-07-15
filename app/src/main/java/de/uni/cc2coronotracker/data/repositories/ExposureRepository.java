package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.data.dao.ExposureDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class ExposureRepository {

    private Context applicationContext;
    private final Executor executor;

    private final ExposureDao exposureDao;

    @Inject()
    public ExposureRepository(@ApplicationContext Context ctx, Executor executor, ExposureDao exposureDao) {
        this.applicationContext = ctx;
        this.executor = executor;
        this.exposureDao = exposureDao;
    }

    public LiveData<List<Exposure>> getExposures() {return exposureDao.getAll();}

    private List<Exposure> getExposures(Contact forContact) {
        return exposureDao.getByContactIDSync(forContact.id);
    }

    public void getExposures(Contact forContact, RepositoryCallback<List<Exposure>> callback) {
        executor.execute(() -> {
            try {
                List<Exposure> c = getExposures(forContact);
                callback.onComplete(new Result.Success<>(c));
            } catch (Exception e) {
                Result errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }

    private long addExposure(Exposure toAdd) { return exposureDao.insert(toAdd); }
    public void addExposure(Exposure toAdd, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(addExposure(toAdd)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
