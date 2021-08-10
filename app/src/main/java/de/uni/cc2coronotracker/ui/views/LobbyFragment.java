package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.LobbyViewModel;


@AndroidEntryPoint
public class LobbyFragment extends Fragment {

    private LobbyViewModel mViewModel;

    @Inject
    public LobbyFragment() {
    }

    public static LobbyFragment newInstance() {
        return new LobbyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lobby_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(LobbyViewModel.class);
    }
}