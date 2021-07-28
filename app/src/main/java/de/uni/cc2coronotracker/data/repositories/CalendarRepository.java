package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.data.dao.ExposureDao;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class CalendarRepository {
    private Context applicationContext;
    private final Executor executor;
    private final ExposureDao exposureDao;

    @Inject()
    public CalendarRepository(@ApplicationContext Context ctx, Executor executor, ExposureDao exposureDao) {
        this.applicationContext = ctx;
        this.executor = executor;
        this.exposureDao = exposureDao;
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
                Result errorResult = new Result.Error<>(e);
                callback.onComplete(errorResult);
            }
        });
    }
}
