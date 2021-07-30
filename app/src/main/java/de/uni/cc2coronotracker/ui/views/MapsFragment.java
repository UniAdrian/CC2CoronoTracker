package de.uni.cc2coronotracker.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.techisfun.android.topsheet.TopSheetBehavior;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

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

    private MarkerOptions defaultMarkerOptions;
    private Marker ownPosition;

    private List<Marker> currentMarkers = new ArrayList<>();

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
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(true);

            String ownLocation = getResources().getString(R.string.map_own_location);
            defaultMarkerOptions = new MarkerOptions()
                    .position(new LatLng(0,0))
                    .draggable(false)
                    .title(ownLocation)
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_baseline_my_location_24))
                    .visible(false);
            ownPosition = map.addMarker(defaultMarkerOptions);

            if (mapsViewModel.ownLocation.getValue() != null) {
                ownPosition.setPosition(mapsViewModel.ownLocation.getValue());
            }

            setupHooks(map);
        }
    };

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        vectorDrawable.setAlpha(255);
        vectorDrawable.setTint(Color.BLACK);

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        TopSheetBehavior<ConstraintLayout> topSheetBehaviour = TopSheetBehavior.from(binding.topSheetInclude.mapsTopSheet);
        topSheetBehaviour.setState(TopSheetBehavior.STATE_COLLAPSED);

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

    @Override
    public void onResume() {
        super.onResume();
        mapsViewModel.startUpdateOwnLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapsViewModel.stopUpdateOwnLocation();
    }

    private void selectContacts() {
        SelectContactDialogFragment.newInstance(true, null).show(this.getParentFragmentManager(), null);
    }

    private void setupHooks(GoogleMap map) {
        mapsViewModel.onMapReady();

        map.setOnMarkerClickListener(marker -> {
            mapsViewModel.updateLocationInfo(marker.getPosition());
            return false;
        });

        mapsViewModel.getGotoPosition().observe(this.getViewLifecycleOwner(), loc -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, ZOOM_STREETS)));

        mapsViewModel.getMarkers().observe(this, markerOptions -> {

            // Remove all old markers
            for (Marker marker : currentMarkers) {
                marker.remove();
            }
            currentMarkers.clear();

            // Add the new ones.
            for (MarkerOptions opt : markerOptions) {
                currentMarkers.add(map.addMarker(opt));
            }
        });

        mapsViewModel.getLocationInfo().observe(this, info -> {
            TopSheetBehavior<ConstraintLayout> topSheetBehaviour = TopSheetBehavior.from(binding.topSheetInclude.mapsTopSheet);
            if (info == null) {
                topSheetBehaviour.setState(TopSheetBehavior.STATE_COLLAPSED);
                return;
            }
            binding.topSheetInclude.setInfo(info);
            topSheetBehaviour.setState(TopSheetBehavior.STATE_EXPANDED);
        });

        mapsViewModel.ownLocation.observe(this, loc -> {
            // Map not ready yet?
            if (map == null || ownPosition == null) {
                return;
            }

            if (loc == null) {
                ownPosition.setVisible(false);
                return;
            }

            ownPosition.setVisible(true);
            ownPosition.setPosition(loc);
        });
    }
}