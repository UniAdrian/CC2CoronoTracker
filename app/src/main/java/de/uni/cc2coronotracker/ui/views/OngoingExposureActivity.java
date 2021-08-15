package de.uni.cc2coronotracker.ui.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.viewmodel.OngoingExposureViewModel;
import de.uni.cc2coronotracker.databinding.ActivityOngoingExposureBinding;
import de.uni.cc2coronotracker.services.OngoingExposureService;

@AndroidEntryPoint
public class OngoingExposureActivity extends AppCompatActivity {

    private final static String TAG = "OngoingExposureActivity";

    private ActivityOngoingExposureBinding binding;
    OngoingExposureViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OngoingExposureViewModel.class);

        binding = ActivityOngoingExposureBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        binding.setVm(viewModel);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        viewModel.currentExposure().observe(this, this::onSetExposure);
        binding.swpBtnCheckInOut.setOnSwipedOffListener(() -> {viewModel.finalizeCurrentExposure(); return null;});

        viewModel.getRequestFinish().observe(this, voidEvent -> {
            Boolean val = voidEvent.getContentIfNotHandled();
            if (val != null && val) {
                stopServiceAndBail();
            }
        });

        prepareFromIntent();
    }

    private void stopServiceAndBail() {
        stopService(new Intent(this, OngoingExposureService.class));
        Log.d(TAG, "StopServiceAndBail");

        finishAndRemoveTask();

        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityIfNeeded(mainActivityIntent, 69420);
    }

    private void prepareFromIntent() {
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("exposure_id")) {
            Log.e(TAG, "Invalid or missing startup parameters.");
            stopServiceAndBail();
            return;
        }

        long exposureId = intent.getLongExtra("exposure_id", -1);
        boolean shouldEnd = intent.getBooleanExtra("exposure_flag_end", false);
        if (shouldEnd) {
            viewModel.endExposureById(exposureId);
        } else {
            viewModel.setExposure(exposureId);
        }
    }

    private void onSetExposure(Exposure exposure) {
        binding.swpBtnCheckInOut.setChecked(true);

        long diffInMillis = new Date().getTime() - exposure.startDate.getTime();
        binding.simpleChronometer.setBase(SystemClock.elapsedRealtime() - diffInMillis);
        binding.simpleChronometer.start();

        startService(exposure);
    }

    private void startService(Exposure exposure) {
        Intent serviceIntent = new Intent(this, OngoingExposureService.class);
        serviceIntent.putExtra("exposure_id", exposure.id);
        serviceIntent.putExtra("exposure_start", exposure.startDate.getTime());
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}