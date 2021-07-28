package de.uni.cc2coronotracker.ui.views;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.dao.ExposureDao;
import de.uni.cc2coronotracker.data.models.Exposure;

import de.uni.cc2coronotracker.data.repositories.CalendarRepository;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;
public class CalendarFragment extends Fragment {

    private Context applicationContext;
    private final Executor executor;
    private MutableLiveData<List<Exposure>> exposures = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();


    private final ExposureDao exposureDao;
    private CalendarViewModel calenderViewModel;


    @Inject()
    public CalendarFragment(@ApplicationContext Context ctx, Executor executor, ExposureDao exposureDao) {
        this.applicationContext = ctx;
        this.executor = executor;
        this.exposureDao = exposureDao;
    }
    CalendarView calendarView;
    TextView calendarText;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        calenderViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        calendarText = view.findViewById(R.id.calendartext);
        Date selectedDate = new Date(calendarView.getDate());
        calenderViewModel.fetchExposure(selectedDate);



        return view;
    }
}