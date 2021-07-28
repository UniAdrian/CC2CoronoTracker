package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.sql.Date;
import java.util.List;

import javax.inject.Inject;

import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.CalendarRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;

public class CalendarViewModel extends ViewModel {

    private final String TAG = "CalendarVM";

    private final ContextMediator ctxMediator;
    private final CalendarRepository calendarRepository;
    private final ExposureRepository exposureRepository;

    private MutableLiveData<List<Exposure>> exposures = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    @Inject
    public CalendarViewModel(ContextMediator ctxMediator, CalendarRepository calendarRepository, ExposureRepository exposureRepository) {
        this.ctxMediator = ctxMediator;
        this.calendarRepository = calendarRepository;
        this.exposureRepository = exposureRepository;
    }
    public void fetchExposure(Date date) {
        calendarRepository.getExposuresByDate(date, queryResult -> {
            isLoading.postValue(false);

            if (queryResult instanceof Result.Success) {
                exposures.postValue(((Result.Success<List<Exposure>>) queryResult).data);
            } else {
                exposures.postValue(null);
                Exception e = ((Result.Error) queryResult).exception;
                Log.e(TAG, "Failed to fetch exposures.", e);
            }

        });
    }

}

