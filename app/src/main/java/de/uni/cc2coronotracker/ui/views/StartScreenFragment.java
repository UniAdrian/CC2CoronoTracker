package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import de.uni.cc2coronotracker.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StartScreenFragment extends Fragment {

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start_screen, container, false);

        Button gtContacts = (Button) view.findViewById(R.id.btnGotoContacts);
        Button gtRQR = (Button) view.findViewById(R.id.btnGotoReadQR);
        Button gtCQR = (Button) view.findViewById(R.id.btnGotoCreateQR);
        Button gtMaps = (Button) view.findViewById(R.id.btnGotoMaps);
        Button gtStatistics = (Button) view.findViewById(R.id.btnGotoStatistics);
        Button gtCalendar = (Button) view.findViewById(R.id.btnGotoCalendar);

        gtContacts.setOnClickListener(this::gotoContacts);
        gtRQR.setOnClickListener(this::gotoReadQR);
        gtCQR.setOnClickListener(this::gotoCreateQR);
        gtMaps.setOnClickListener(this::gotoMaps);
        gtStatistics.setOnClickListener(this::gotoStatistics);
        gtCalendar.setOnClickListener(this::gotoCalendar);

        return view;
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
}