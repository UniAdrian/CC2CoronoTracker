package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.dao.StatisticsDao;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.StatisticsRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private static final String TAG = "StatisticsVM";

    public enum EXPOSURE_RANGE {
        DAY,
        WEEK,
        MONTH,
    }

    private final ExposureRepository exposureRepository;
    private final StatisticsRepository statisticsRepository;

    MutableLiveData<EXPOSURE_RANGE> exposureRange = new MutableLiveData<>();
    MutableLiveData<ExposureRangeDataSet> exposuresByRange = new MutableLiveData<>();

    @Inject
    public StatisticsViewModel(StatisticsRepository statisticsRepository, ExposureRepository exposureRepository) {
        this.exposureRepository = exposureRepository;
        this.statisticsRepository = statisticsRepository;

        updateExposures(EXPOSURE_RANGE.MONTH);
    }

    public void updateExposures(EXPOSURE_RANGE range) {
        // Don't do unnecessary work.
        if (range == exposureRange.getValue()) return;
        exposureRange.postValue(range);

        java.sql.Date leastDate;
        switch (range) {
            case DAY:
                leastDate = last24h();
                break;

            case WEEK:
                leastDate = lastWeek();
                break;
            case MONTH:
                leastDate = lastMonth();
                break;

            default:
                leastDate = new java.sql.Date(0);
        }

        statisticsRepository.getExposuresByDay(leastDate, result -> {
            if (result instanceof Result.Success) {
                exposuresByRange.postValue(processNumExposuresByDay(((Result.Success<List<StatisticsDao.NumExposuresByDay>>) result).data, leastDate));
            } else {
                Log.e(TAG, "Failed querying exposuresByDay.", ((Result.Error)result).exception);
            }
        });
    }

    private ExposureRangeDataSet processNumExposuresByDay(List<StatisticsDao.NumExposuresByDay> data, Date leastDate) {

        ExposureRangeDataSet result = new ExposureRangeDataSet();
        if (data == null) {
            return result;
        }

        List<Entry> barEntries = new ArrayList<>();
        for (StatisticsDao.NumExposuresByDay entry : data) {

            Entry barEntry = new Entry(entry.dayDiff, entry.numExposures);
            barEntries.add(barEntry);
        }

        List<String> labels = prepLabelString(leastDate);

        result.data = new LineData(new LineDataSet(barEntries, "Exposures"));
        result.labels = labels;

        return result;
    }

    private List<String> prepLabelString(Date leastDate) {
        Date endDate = new Date();
        long numDays = TimeUnit.DAYS.convert(endDate.getTime() - leastDate.getTime(), TimeUnit.MILLISECONDS);

        SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd");

        List<String> labels = new ArrayList<>((int) numDays);
        for (int i = 0; i < numDays; ++i) {
            Date current = DateUtils.addDays(leastDate, i);
            labels.add(df.format(current));
        }

        return labels;
    }


    private java.sql.Date last24h() {
        Date result = DateUtils.addDays(new Date(),-1);
        return new java.sql.Date(result.getTime());
    }

    private java.sql.Date lastWeek() {
        Date result = DateUtils.addWeeks(new Date(),-1);
        return new java.sql.Date(result.getTime());
    }

    private java.sql.Date lastMonth() {
        Date result = DateUtils.addMonths(new Date(),-1);
        return new java.sql.Date(result.getTime());
    }


    public LiveData<EXPOSURE_RANGE> getExposureRange() {
        return exposureRange;
    }
    public LiveData<ExposureRangeDataSet> getExposureByRangeEntries() {
        return exposuresByRange;
    }

    public static class ExposureRangeDataSet {
        public List<String> labels;
        public LineData data;
    }
}