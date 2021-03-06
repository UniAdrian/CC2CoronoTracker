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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;
import de.uni.cc2coronotracker.databinding.FragmentExposureMapsBinding;

public class ExposureMapFragment extends Fragment {

    private final String TAG = "Exposure Map";
    private CalendarViewModel calendarViewModel;
    private CalendarViewModel.ExposureDisplayInfo selectedInfo;
    private FragmentExposureMapsBinding binding;
    private List<MarkerOptions> markerOptionsList = new ArrayList<>();
    private MarkerOptions selectedMarkerOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            MapsInitializer.initialize(getActivity().getApplicationContext());
            calendarViewModel = new ViewModelProvider(this.getActivity()).get(CalendarViewModel.class);
            ExposureMapFragmentArgs exposureMapFragmentArgs = ExposureMapFragmentArgs.fromBundle(getArguments());
            long exposureId = exposureMapFragmentArgs.getExposureId();
            selectedInfo = calendarViewModel.getExposureInfoById(exposureId);
            selectedMarkerOptions = generateMarkerOptions(selectedInfo, BitmapDescriptorFactory.HUE_GREEN);
            calendarViewModel.getExposureInfo().observe(this, exposureInfos -> {
                for (CalendarViewModel.ExposureDisplayInfo info : exposureInfos) {
                    if (info.exposureData.id != exposureId) {
                        MarkerOptions options = generateMarkerOptions(info, BitmapDescriptorFactory.HUE_BLUE);
                        if (options != null)
                            markerOptionsList.add(options);
                    }
                }
            });
        } else {
            Log.e(TAG, "getActivity returned null.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_exposure_maps, container, false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        public void onMapReady(GoogleMap map) {
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            addMarkers(map, markerOptionsList);
            Marker marker = map.addMarker(selectedMarkerOptions);
            if (marker != null)
                marker.showInfoWindow();
            if (selectedInfo.exposureData.location != null)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedInfo.exposureData.location, 20.0f));
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.exposureMapContainer);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
        private MarkerOptions generateMarkerOptions (CalendarViewModel.ExposureDisplayInfo info, float hue) {
            LatLng location = info.exposureData.location;
            String title = String.format("%s", info.contactName);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
            String time  = dateFormat.format(info.exposureData.startDate);
            String snippet = String.format("on %s, at %s", info.exposureData.startDate, time);
            MarkerOptions options;
            if (location != null) {
                options = new MarkerOptions().position(location)
                        .title(title).snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(hue));
                return options;
            }
        return null;
    }
    private void addMarkers(GoogleMap map, List<MarkerOptions> options) {
        for (MarkerOptions option : options) {
            map.addMarker(option);
        }
    }
};

