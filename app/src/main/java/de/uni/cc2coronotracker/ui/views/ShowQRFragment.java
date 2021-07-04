package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.ShowQRViewModel;
import de.uni.cc2coronotracker.databinding.FragmentShowQrBinding;

/**
 * Displays (and generates if needed) a QR code for this user.
 */
@AndroidEntryPoint
public class ShowQRFragment extends Fragment {

    private ShowQRViewModel showQRViewModel;
    private FragmentShowQrBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showQRViewModel = new ViewModelProvider(this).get(ShowQRViewModel.class);

        showQRViewModel.getQRCode().observe(this, bitmap -> {
            Log.d("SQR", "Got bitmap: " + bitmap);
            binding.imgQR.setImageBitmap(bitmap);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_show_qr, container, false);

        binding.setQrVM(showQRViewModel);
        binding.setLifecycleOwner(this);

        binding.imgQR.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int smallDim = Math.min(binding.imgQR.getHeight(), binding.imgQR.getWidth());
            Log.d("SQR", "Creating QR code with dimension: " + smallDim);
            showQRViewModel.createOrGetQRCode("Test, test, test!", smallDim);
        });

        View view = binding.getRoot();
        return view;
    }

}