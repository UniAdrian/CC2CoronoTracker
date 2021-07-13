package de.uni.cc2coronotracker.ui.views;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Collections;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.viewmodel.ReadQRViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
import de.uni.cc2coronotracker.databinding.FragmentReadQrBinding;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;


@AndroidEntryPoint
public class ReadQRFragment extends Fragment {

    private CodeScanner codeScanner;
    private FragmentReadQrBinding binding;

    private ReadQRViewModel readQRViewModel;
    private ContactSelectionDialogViewModel contactSelectionViewModel;

    @Inject
    public ContextMediator ctxMediator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readQRViewModel = new ViewModelProvider(this).get(ReadQRViewModel.class);
        contactSelectionViewModel = new ViewModelProvider(getActivity()).get(ContactSelectionDialogViewModel.class);

        contactSelectionViewModel.getOnContactSelection().observe(this, contactPickEvent -> {
            readQRViewModel.handleContactPick(contactPickEvent);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_read_qr, container, false);
        binding.setLifecycleOwner(this);

        codeScanner = new CodeScanner(getContext(), binding.scannerView);

        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));

        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setScanMode(ScanMode.SINGLE);
        codeScanner.setAutoFocusEnabled(true);

        codeScanner.setErrorCallback(error -> {
            Log.e("err", "Read QR Error: " + error.getMessage());
            codeScanner.startPreview();
        });

        codeScanner.setDecodeCallback(result -> requireActivity().runOnUiThread(() -> {
            try {
                QrIntent.Intent intent = QrIntent.fromString(result.getText());
                readQRViewModel.handleQRIntent(intent);
            } catch (Exception e) {
                ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.qr_scan_failed, Snackbar.LENGTH_LONG, e.getMessage()));
            }
        }));

        binding.scannerView.setOnClickListener(v -> codeScanner.startPreview());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        requestForCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        codeScanner.releaseResources();
    }

    private void requestForCamera() {
        Dexter.withContext(getContext()).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                codeScanner.startPreview();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(getActivity(),"Camera Permission is Required",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();
    }
}
