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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ExposureListAdapter;
import de.uni.cc2coronotracker.data.adapters.IncidenceHistoryAdapter;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;
import de.uni.cc2coronotracker.data.viewmodel.IncidenceHistoryViewModel;

public class IncidenceHistoryFragment extends Fragment {
    private IncidenceHistoryViewModel incidenceHistoryViewModel;

    private RecyclerView recyclerView;
    private TextView textView;

    @Inject()
    public IncidenceHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getActivity() == null) return;
        incidenceHistoryViewModel = new ViewModelProvider(this.getActivity()).get(IncidenceHistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidence_history, container, false);

        textView = view.findViewById(R.id.incidenceTextView);
        recyclerView = view.findViewById(R.id.incidenceListRV);
        recyclerView.setAdapter(new ExposureListAdapter(new ArrayList<>(), getContext(), null));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        incidenceHistoryViewModel.fetchExposures();
        incidenceHistoryViewModel.getConsolidatedList().observe(getViewLifecycleOwner(), consolidatedList -> {
            if (consolidatedList.size() == 0) {
                textView.setText("No exposures");
                recyclerView.setAdapter(new IncidenceHistoryAdapter(new ArrayList<>(), getContext()));
            } else {
                recyclerView.setAdapter(new IncidenceHistoryAdapter(consolidatedList, getContext()));
            }
        });
        return view;
    }
}