package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.sql.Date;
import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private CalendarViewModel calenderViewModel;

    CalendarView calendarView;
    TextView calendarText;
    Date selectedDate;

    @Inject()
    public CalendarFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calenderViewModel = new ViewModelProvider(this.getActivity()).get(CalendarViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        calendarText = view.findViewById(R.id.calendartext);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int mm, int dd) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, mm);
                calendar.set(Calendar.DAY_OF_MONTH, dd);
                Long date = calendar.getTimeInMillis();
                selectedDate = new Date(date);
                calenderViewModel.fetchExposure(selectedDate);
            }
        });
        return view;
    }
}