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

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.qr.EGC;
import de.uni.cc2coronotracker.data.viewmodel.CertificateViewModel;
import de.uni.cc2coronotracker.databinding.CertificateFragmentBinding;

public class CertificateFragment extends Fragment {

    CertificateFragmentBinding binding;

    private CertificateViewModel certViewModel;
    public static CertificateFragment newInstance() {
        return new CertificateFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        EGC certificate = CertificateFragmentArgs.fromBundle(getArguments()).getCertificate();
        binding =  DataBindingUtil.inflate(inflater, R.layout.certificate_fragment, container, false);

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setEgc(certificate);
        binding.setCertificate(certificate.certificates.get(0));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        certViewModel = new ViewModelProvider(this.getActivity()).get(CertificateViewModel.class);
        certViewModel.setCurrentCertificate(binding.getEgc());
    }
}