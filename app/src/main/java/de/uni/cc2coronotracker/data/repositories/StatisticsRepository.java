package de.uni.cc2coronotracker.data.repositories;

import android.content.Context;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.data.dao.StatisticsDao;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class StatisticsRepository {

    private final Context context;
    private final Executor executor;
    private final StatisticsDao dao;

    @Inject
    public StatisticsRepository(@ApplicationContext Context ctx, Executor executor, StatisticsDao statisticsDao) {
        this.context = ctx;
        this.executor = executor;
        this.dao = statisticsDao;
    }


    public void getExposuresByDay(Date leastDate, RepositoryCallback<List<StatisticsDao.NumExposuresByDay>> callback) {
        executor.execute(() -> {
            try {
                final List<StatisticsDao.NumExposuresByDay> exposuresByDay = dao.getExposuresByDay(leastDate);
                callback.onComplete(new Result.Success<>(exposuresByDay));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
