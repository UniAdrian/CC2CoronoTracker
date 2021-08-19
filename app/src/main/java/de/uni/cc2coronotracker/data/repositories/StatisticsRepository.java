package de.uni.cc2coronotracker.data.repositories;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import de.uni.cc2coronotracker.data.dao.StatisticsDao;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;

/**
 * Fetches accumulated information of raw data for statistics.
 * Read only repository.
 */
public class StatisticsRepository {

    private final Executor executor;
    private final StatisticsDao dao;

    @Inject
    public StatisticsRepository(Executor executor, StatisticsDao statisticsDao) {
        this.executor = executor;
        this.dao = statisticsDao;
    }


    /**
     * Fetches all exposures between today and {@code leastDate} accumulated by day. Also contains metadata.
     * @see  {@link de.uni.cc2coronotracker.data.dao.StatisticsDao.NumExposuresByDay}
     * @param leastDate The least possible date
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
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

    /**
     * Fetches all exposures accumulated by contact. Also fetches some metadata.
     * @see {@link de.uni.cc2coronotracker.data.dao.StatisticsDao.NumExposuresByContact}
     * @param minPercent The minimum percent the contact has to have of all results to be included
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
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

    /**
     * Fetches general statistics for contacts and exposures like e.g. total num of exposures.
     * @see {@link de.uni.cc2coronotracker.data.dao.StatisticsDao.GeneralExposureInfo}
     * @param callback The {@link RepositoryCallback} to be called when done.
     */
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
