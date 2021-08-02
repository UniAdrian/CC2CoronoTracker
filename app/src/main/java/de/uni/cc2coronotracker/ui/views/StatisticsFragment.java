package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.StatisticsViewModel;
import de.uni.cc2coronotracker.databinding.StatisticsFragmentBinding;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {

    private static final String TAG = "Statistics";


    private StatisticsViewModel mViewModel;
    private StatisticsFragmentBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.statistics_fragment, container, false);
        binding.setLifecycleOwner(this);
        binding.setVm(mViewModel);
        binding.setFrag(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.chrtExposuresByContacts.getDescription().setEnabled(false);


        binding.chrtExposuresLastN.getAxisRight().setEnabled(false);
        binding.chrtExposuresLastN.getDescription().setEnabled(false);

        binding.chrtExposuresLastN.animateY( 750);

        XAxis xAxis = binding.chrtExposuresLastN.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);

        YAxis yAxis = binding.chrtExposuresLastN.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });


        mViewModel.getExposureByRangeEntries().observe(this.getViewLifecycleOwner(), exposureEntries -> {
            if (exposureEntries == null) return;

            // Prevents a very, very rare NPE
            if (binding.chrtExposuresLastN == null) {
                Log.w(TAG, "Unable to set exposure data, graph unavailable.");
                return;
            }

            exposureEntries.data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getPointLabel(Entry entry) {
                    return String.valueOf((int)entry.getY());
                }
            });

            xAxis.setValueFormatter(new IndexAxisValueFormatter(exposureEntries.labels));

            ((LineDataSet)exposureEntries.data.getDataSetByIndex(0)).setMode(LineDataSet.Mode.CUBIC_BEZIER);
            binding.chrtExposuresLastN.setData(exposureEntries.data);
            binding.chrtExposuresLastN.fitScreen();
            binding.chrtExposuresLastN.animateY(750);
        });

        mViewModel.getExposuresByContact().observe(this.getViewLifecycleOwner(), pieDataSet -> {
            if (pieDataSet == null) return;
            // Prevents a very, very rare NPE
            if (binding.chrtExposuresLastN == null) {
                Log.w(TAG, "Unable to set exposure data, graph unavailable.");
                return;
            }
            binding.chrtExposuresByContacts.setData(pieDataSet);
            binding.chrtExposuresByContacts.invalidate();
        });

        mViewModel.updateGeneralExposureInfo();
        mViewModel.updateExposuresByContact();

    }
}