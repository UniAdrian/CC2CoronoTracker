package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.dao.StatisticsDao;
import de.uni.cc2coronotracker.data.repositories.StatisticsRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.ResourceProvider;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private static final String TAG = "StatisticsVM";

    public enum EXPOSURE_RANGE {
        DAY,
        WEEK,
        MONTH,
    }

    private final StatisticsRepository statisticsRepository;
    private final ResourceProvider resourceProvider;

    private MutableLiveData<EXPOSURE_RANGE> exposureRange = new MutableLiveData<>();
    private MutableLiveData<ExposureRangeDataSet> exposuresByRange = new MutableLiveData<>();
    private MutableLiveData<PieData> exposuresByContact = new MutableLiveData<>();
    private MutableLiveData<StatisticsDao.GeneralExposureInfo> generalExposureInfo = new MutableLiveData<>();

    @Inject
    public StatisticsViewModel(StatisticsRepository statisticsRepository, ResourceProvider resourceProvider) {
        this.statisticsRepository = statisticsRepository;
        this.resourceProvider = resourceProvider;

        updateExposuresByDay(EXPOSURE_RANGE.MONTH);
        updateExposuresByContact();
        updateGeneralExposureInfo();
    }

    public void updateGeneralExposureInfo() {
        statisticsRepository.getGeneralExposureInfo(result -> {
            if (result instanceof Result.Success) {
                StatisticsDao.GeneralExposureInfo data = ((Result.Success<StatisticsDao.GeneralExposureInfo>) result).data;
                generalExposureInfo.postValue(data);
            } else {
                Log.e(TAG, "Failed to fetch exposures by contact", ((Result.Error)result).exception);
                exposuresByContact.postValue(null);
            }
        });
    }

    public void updateExposuresByContact() {
        statisticsRepository.getExposuresByContact(0.05f, result -> {
            if (result instanceof Result.Success) {
                List<StatisticsDao.NumExposuresByContact> data = ((Result.Success<List<StatisticsDao.NumExposuresByContact>>) result).data;
                exposuresByContact.postValue(processExposuresByContact(data));
            } else {
                Log.e(TAG, "Failed to fetch exposures by contact", ((Result.Error)result).exception);
                exposuresByContact.postValue(null);
            }
        });
    }

    private PieData processExposuresByContact(List<StatisticsDao.NumExposuresByContact> data) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "Exposures by contact";

        // We fetch those anew every time so we do not have to react actively to night-mode changes.
        // This call happens infrequently enough to not be a performance hit.
        int[] colors = resourceProvider.getColors(R.array.statistics_colors);

        //input data and fit data into pie chart entry
        long n = 0;
        for(StatisticsDao.NumExposuresByContact entry : data){
            n+=entry.nExposures;
            pieEntries.add(new PieEntry(entry.nExposures, entry.label));
        }

        if (data.get(0).nTotalExposures - n > 0) {
            pieEntries.add(new PieEntry(data.get(0).nTotalExposures - n, "Other"));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, label);
        dataSet.setColors(colors);

        return new PieData(dataSet);
    }

    public void updateExposuresByDay(EXPOSURE_RANGE range) {
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

    public LiveData<PieData> getExposuresByContact() {
        return exposuresByContact;
    }

    public LiveData<StatisticsDao.GeneralExposureInfo> getGeneralExposureInfo() {
        return generalExposureInfo;
    }

    public static class ExposureRangeDataSet {
        public List<String> labels;
        public LineData data;
    }
}