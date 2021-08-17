package de.uni.cc2coronotracker.ui.views;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ExposureListAdapter;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private CalendarViewModel calenderViewModel;

    CalendarView calendarView;
    TextView calendarText;
    Date selectedDate;
    RecyclerView recyclerView;
    TextView exposureListTitle;
    TextView exposureView;

    @Inject()
    public CalendarFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getActivity() == null) return;
        calenderViewModel = new ViewModelProvider(this.getActivity()).get(CalendarViewModel.class);

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        calendarText = view.findViewById(R.id.calendartext);
        exposureListTitle = (TextView) view.findViewById(R.id.exposureListTitle);
        exposureView = view.findViewById(R.id.exposureView);
        recyclerView = view.findViewById(R.id.exposureListRV);
        recyclerView.setAdapter(new ExposureListAdapter(new ArrayList<>(), getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        calenderViewModel.getExposureInfo().observe(this.getViewLifecycleOwner(), exposuresInfo -> {
            if(exposuresInfo.size() == 0) {
                exposureView.setText("No exposures on this day!");
                recyclerView.setAdapter(new ExposureListAdapter(new ArrayList<>(), getContext()));
            }
            else {
                exposureView.setText("");
                recyclerView.setAdapter(new ExposureListAdapter(exposuresInfo, getContext()));
            }
        });

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        calenderViewModel.fetchExposure(new Date(cal.getTimeInMillis()));

        calendarView.setOnDateChangeListener((calendarView, year1, mm, dd) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(0);
            calendar.set(Calendar.YEAR, year1);
            calendar.set(Calendar.MONTH, mm);
            calendar.set(Calendar.DAY_OF_MONTH, dd);
            Long date = calendar.getTimeInMillis();
            selectedDate = new Date(date);
            exposureListTitle.setText(String.format("Exposures on: %s", selectedDate.toString()));
            calenderViewModel.fetchExposure(selectedDate);
        });
        return view;
    }
}