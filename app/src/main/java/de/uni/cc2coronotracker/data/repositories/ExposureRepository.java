package de.uni.cc2coronotracker.data.repositories;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import de.uni.cc2coronotracker.data.dao.ExposureDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class ExposureRepository {

    private final Executor executor;

    private final ExposureDao exposureDao;

    @Inject()
    public ExposureRepository(Executor executor, ExposureDao exposureDao) {
        this.executor = executor;
        this.exposureDao = exposureDao;
    }

    private List<Exposure> getExposures(Contact forContact) {return exposureDao.getByContactIDSync(forContact.id);}
    public void getExposures(Contact forContact, RepositoryCallback<List<Exposure>> callback) {
        executor.execute(() -> {
            try {
                List<Exposure> c = getExposures(forContact);
                callback.onComplete(new Result.Success<>(c));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
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

    private List<Exposure> getExposuresByDate(Date date) {
        return exposureDao.getExposuresByDate(date);
    }
    public void getExposuresByDate(Date date, RepositoryCallback<List<Exposure>> callback) {
        executor.execute(() -> {
            try {
                List<Exposure> c = getExposuresByDate(date);
                callback.onComplete(new Result.Success<>(c));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }


    public void getExposure(long exposureId, RepositoryCallback<Exposure> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(exposureDao.getById(exposureId)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    public void updateExposure(Exposure exposure, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                if (exposure.id < 1)
                    throw new IllegalArgumentException("Exposure must have valid ID.");
                exposureDao.update(exposure);
                callback.onComplete(new Result.Success<>(null));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
