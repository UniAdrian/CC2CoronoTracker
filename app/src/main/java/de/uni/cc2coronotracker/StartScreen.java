package de.uni.cc2coronotracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StartScreen extends Fragment {

    public StartScreen() {
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

        gtContacts.setOnClickListener(this::gotoContacts);
        gtRQR.setOnClickListener(this::gotoReadQR);
        gtCQR.setOnClickListener(this::gotoCreateQR);

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
}