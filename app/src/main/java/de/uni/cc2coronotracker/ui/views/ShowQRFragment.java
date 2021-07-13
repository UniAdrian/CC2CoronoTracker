package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.PreferencesViewModel;
import de.uni.cc2coronotracker.databinding.FragmentShowQrBinding;

/**
 * Displays (and generates if needed) a QR code for this user.
 */
@AndroidEntryPoint
public class ShowQRFragment extends Fragment {

    private PreferencesViewModel preferencesViewModel;
    private FragmentShowQrBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesViewModel = new ViewModelProvider(this).get(PreferencesViewModel.class);

        preferencesViewModel.getQRCode().observe(this, bitmap -> {
            binding.imgQR.setImageBitmap(bitmap);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_show_qr, container, false);

        binding.setQrVM(preferencesViewModel);
        binding.setLifecycleOwner(this);

        binding.imgQR.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int smallDim = Math.min(binding.imgQR.getHeight(), binding.imgQR.getWidth());

                preferencesViewModel.createOrGetPersonalQRCode(smallDim);
                binding.imgQR.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        View view = binding.getRoot();
        return view;
    }

}