package de.uni.cc2coronotracker.data.repositories;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import de.uni.cc2coronotracker.data.dao.StatisticsDao;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

public class StatisticsRepository {

    private final Executor executor;
    private final StatisticsDao dao;

    @Inject
    public StatisticsRepository(Executor executor, StatisticsDao statisticsDao) {
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

    public void getExposuresByContact(float minPercent, RepositoryCallback<List<StatisticsDao.NumExposuresByContact>> callback) {
        executor.execute(() -> {
            try {
                List<StatisticsDao.NumExposuresByContact> exposuresByContact = dao.getExposuresByContact(minPercent);
                callback.onComplete(new Result.Success<>(exposuresByContact));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }

    public void getGeneralExposureInfo(RepositoryCallback<StatisticsDao.GeneralExposureInfo> callback) {
        executor.execute(() -> {
            try {
                callback.onComplete(new Result.Success<>(dao.getGeneralExposureInfo()));
            } catch (Exception e) {
                callback.onComplete(new Result.Error<>(e));
            }
        });
    }
}
