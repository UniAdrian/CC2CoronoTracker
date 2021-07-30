package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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


        mViewModel.getBarEntries().observe(this.getViewLifecycleOwner(), barEntries -> {
            binding.chrtExposuresLastN.setData(barEntries);
        });

        return binding.getRoot();
    }
}