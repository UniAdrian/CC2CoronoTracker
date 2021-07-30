package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private static final String TAG = "StatisticsVM";

    public enum EXPOSURE_RANGE {
        DAY,
        WEEK,
        MONTH,
        ALWAYS
    }

    private final ExposureRepository exposureRepository;

    MutableLiveData<EXPOSURE_RANGE> exposureRange = new MutableLiveData<>(EXPOSURE_RANGE.ALWAYS);
    MutableLiveData<BarData> exposuresByRange = new MutableLiveData<>();

    @Inject
    public StatisticsViewModel(ExposureRepository exposureRepository) {
        this.exposureRepository = exposureRepository;
    }

    public void updateExposures(EXPOSURE_RANGE range) {
        // Dont do unecessary work.
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

        exposureRepository.getExposuresAfter(leastDate, result -> {
            if (result instanceof Result.Success) {
                BarData data = processExposuresToBar(((Result.Success<List<Exposure>>) result).data, leastDate);
                exposuresByRange.postValue(data);
            } else {
                exposuresByRange.postValue(null);
            }
        });
    }

    private BarData processExposuresToBar(List<Exposure> data, Date leastDate) {

        BarData result = new BarData();
        if (data == null) {
            return result;
        }
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

        Log.d(TAG, "#Exposures: " + data.size());


        Map<String, Integer> numberOfExposuresByDay = new HashMap<>();
        for (Exposure exposure : data) {
            String day = formater.format(exposure.date);

            int n = 0;
            // We do not have access to getOrDefault unless we use a higher API level
            if (numberOfExposuresByDay.containsKey(day))
                n = numberOfExposuresByDay.get(day);

            numberOfExposuresByDay.put(day, n+1);
        }

        int i=0;
        List<BarEntry> barEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : numberOfExposuresByDay.entrySet()) {
            BarEntry barEntry = new BarEntry(i, entry.getValue());
            barEntries.add(barEntry);
            ++i;
        }

        result.addDataSet(new BarDataSet(barEntries, "Exposures"));
        return result;
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

    public LiveData<BarData> getBarEntries() {
        return exposuresByRange;
    }
}