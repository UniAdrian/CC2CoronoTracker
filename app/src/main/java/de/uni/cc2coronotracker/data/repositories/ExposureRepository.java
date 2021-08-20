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

/**
 * Single source of truth for exposure data.
 */
public class ExposureRepository {

    private final Executor executor;
    private final ExposureDao exposureDao;

    @Inject()
    public ExposureRepository(Executor executor, ExposureDao exposureDao) {
        this.executor = executor;
        this.exposureDao = exposureDao;
    }

    /**
     * Returns a list of exposures for a given contact
     * @param forContact The contact associated with the exposures
     * @return The List of fetched exposures
     */
    private List<Exposure> getExposures(Contact forContact) {return exposureDao.getByContactIDSync(forContact.id);}

    /**
     * Returns a list of exposures for a given contact
     * @param forContact The contact associated with the exposures
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
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

    /**
     * Adds the given exposures to the database.
     * @param toAdd The exposures to add
     * @return
     */
    private long addExposure(Exposure toAdd) { return exposureDao.insert(toAdd); }

    /**
     * Adds the given exposures to the database.
     * @param toAdd The exposures to add
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void addExposure(Exposure toAdd, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(addExposure(toAdd)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Fetch all exposures for a given date
     * @param date The date
     * @return
     */
    private List<Exposure> getExposuresByDate(Date date) {
        return exposureDao.getExposuresByDate(date);
    }

    /**
     * Fetch all exposures for a given date
     * @param date The date
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
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


    /**
     * Fetch a single exposure by unique ID
     * @param exposureId The unique exposure ID
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
    public void getExposure(long exposureId, RepositoryCallback<Exposure> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(exposureDao.getById(exposureId)));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    /**
     * Updates an exposure with the given values
     * @param exposure The updated exposure
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
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
