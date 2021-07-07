package de.uni.cc2coronotracker.ui.views;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Scanner;

import de.uni.cc2coronotracker.R;


public class ReadQRFragment extends Fragment {

    public ReadQRFragment() {

    }

    CodeScanner codeScanner;
    CodeScannerView scannerView;
    TextView resultData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_qr, container, false);
        scannerView = view.findViewById(R.id.scannerView);
        resultData = view.findViewById(R.id.resultsOfScan);
        codeScanner = new CodeScanner(view.getContext(), scannerView);
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Code for Validation need to be done instead the QR code value is displayed in TextView
                        resultData.setText(result.getText());
                    }
                });

                scannerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        codeScanner.startPreview();

                    }
                });

            }
        });return view;
    }

    private void runOnUiThread(Runnable runnable) {
    }

    @Override
    public void onResume() {
        super.onResume();
        requestForCamera();
    }

    private void requestForCamera() {
        Dexter.withContext(scannerView.getContext()).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
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
