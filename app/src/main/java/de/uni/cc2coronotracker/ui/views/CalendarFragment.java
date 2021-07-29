package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.sql.Date;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private CalendarViewModel calenderViewModel;

    CalendarView calendarView;
    TextView calendarText;

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
        Date selectedDate = new Date(calendarView.getDate());
        calenderViewModel.fetchExposure(selectedDate);

        return view;
    }
}