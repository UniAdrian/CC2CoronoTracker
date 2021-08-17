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

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.CertificateAdapter;
import de.uni.cc2coronotracker.data.models.CertEntity;
import de.uni.cc2coronotracker.data.viewmodel.CertificateListViewModel;
import de.uni.cc2coronotracker.databinding.CertificateListFragmentBinding;

@AndroidEntryPoint
public class CertificateListFragment extends Fragment {
    private CertificateListViewModel viewModel;
    private CertificateListFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (this.getActivity() == null) return null;

        viewModel = new ViewModelProvider(this.getActivity()).get(CertificateListViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.certificate_list_fragment, container, false);
        binding.setLifecycleOwner(this.getViewLifecycleOwner());
        binding.setVm(viewModel);

        binding.rvCerts.setAdapter(new CertificateAdapter(new ArrayList<>(), item -> {/*Do nothing*/}));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getCerts().observe(getViewLifecycleOwner(), certEntities -> binding.rvCerts.setAdapter(new CertificateAdapter(certEntities, this::onCertPicked)));
    }

    public void onCertPicked(CertEntity cert) {
        viewModel.gotoDetails(cert);
    }
}