package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.databinding.FragmentStartScreenBinding;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StartScreenFragment extends Fragment {

    private FragmentStartScreenBinding binding;

    public StartScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start_screen, container, false);

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setFrag(this);

        return binding.getRoot();
    }

    public void gotoContacts(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_contacts);
    }

    public void gotoReadQR(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_readQR);
    }

    public void gotoCreateQR(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_showQR);
    }

    public void gotoMaps(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_mapsFragment);
    }

    public void gotoStatistics(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_statistics);
    }

    public void gotoCalendar(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_calendarFragment);
    }

    public void gotoLobby(View view) {
        Navigation.findNavController(view).navigate(R.id.action_startScreen_to_lobbyFragment);
    }
}