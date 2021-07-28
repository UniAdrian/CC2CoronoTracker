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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.MapsViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
import de.uni.cc2coronotracker.databinding.FragmentMapsBinding;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.ui.dialogs.SelectContactDialogFragment;

@AndroidEntryPoint
public class MapsFragment extends Fragment {

    private final String TAG = "Maps";

    private final float ZOOM_WORLD = 1.0f;
    private final float ZOOM_CONTINENT = 5.0f;
    private final float ZOOM_CITY = 10.0f;
    private final float ZOOM_STREETS = 15.0f;
    private final float ZOOM_BUILDINGS = 20.0f;

    private MapsViewModel mapsViewModel;
    private ContactSelectionDialogViewModel contactSelectionViewModel;

    private FragmentMapsBinding binding;

    @Inject
    public ContextMediator ctxMediator;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap map) {
            LatLng kassel = new LatLng(51.312801, 9.481544);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(kassel, ZOOM_CITY));
            map.getUiSettings().setZoomControlsEnabled(true);

            setupHooks(map);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "On Create");

        // Should never happen, but you never know - Makes the warning disappear anyway.
        if (getActivity() != null) {
            // Setup view models...
            mapsViewModel = new ViewModelProvider(this.getActivity()).get(MapsViewModel.class);

            contactSelectionViewModel = new ViewModelProvider(this.getActivity()).get(ContactSelectionDialogViewModel.class);
            contactSelectionViewModel.getOnContactSelection().observe(this, event -> {
                ContactSelectionDialogViewModel.ContactIntentTuple contentIfNotHandled = event.getContentIfNotHandled();
                if (contentIfNotHandled != null) {
                    mapsViewModel.DisplayMarkersForContacts(contentIfNotHandled.contactList);
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false);

        binding.setMapsVM(mapsViewModel);
        binding.setLifecycleOwner(this);

        setupBottomSheet();

        return binding.getRoot();
    }

    private void setupBottomSheet() {
        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(binding.bottomSheetInclude.mapsBottomSheet);
        sheetBehavior.setHideable(false);

        binding.bottomSheetInclude.mapsConfigTitle.setOnClickListener(v -> {
            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        binding.bottomSheetInclude.mapsSelectContacts.setOnClickListener(v-> {
            selectContacts();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void selectContacts() {
        SelectContactDialogFragment.newInstance(true, null).show(this.getParentFragmentManager(), null);
    }

    private void setupHooks(GoogleMap map) {
        mapsViewModel.onMapReady();
        mapsViewModel.getGotoPosition().observe(this.getViewLifecycleOwner(), loc -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, ZOOM_CITY)));

        mapsViewModel.getMarkers().observe(this, markerOptions -> {
            map.clear();

            for (MarkerOptions opt : markerOptions) {
                map.addMarker(opt);
            }
        });
    }
}