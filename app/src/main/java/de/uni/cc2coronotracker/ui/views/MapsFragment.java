package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.MapsViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
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

        // Should never happen, but you never know
        if (getActivity()!=null) {
            mapsViewModel = new ViewModelProvider(this.getActivity()).get(MapsViewModel.class);
            contactSelectionViewModel = new ViewModelProvider(this.getActivity()).get(ContactSelectionDialogViewModel.class);
            contactSelectionViewModel.getOnContactSelection().observe(this, event -> {
                ContactSelectionDialogViewModel.ContactIntentTuple contentIfNotHandled = event.getContentIfNotHandled();
                if (contentIfNotHandled != null) {
                    mapsViewModel.selectContacts(contentIfNotHandled.contactList);
                }
            });
        } else {
            Log.e(TAG, "getActivity returned null.");
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.maps_menu, menu);

        final MenuItem selectContacts = menu.findItem(R.id.maps_select_contacts);
        selectContacts.setOnMenuItemClickListener(this::selectContacts);
    }

    private boolean selectContacts(MenuItem mitem) {
        SelectContactDialogFragment.newInstance(true, null).show(this.getParentFragmentManager(), null);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setupHooks(GoogleMap map) {
        mapsViewModel.onMapReady();
        mapsViewModel.getGotoPosition().observe(this.getViewLifecycleOwner(), loc -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, ZOOM_STREETS)));

        mapsViewModel.getMarkers().observe(this, markerOptions -> {
            if (markerOptions == null) {
                map.clear();
                return;
            }

            for (MarkerOptions opt : markerOptions) {
                map.addMarker(opt);
            }
        });
    }
}