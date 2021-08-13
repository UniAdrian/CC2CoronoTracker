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
import androidx.transition.TransitionManager;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.LobbyExposureAdapter;
import de.uni.cc2coronotracker.data.viewmodel.LobbyViewModel;
import de.uni.cc2coronotracker.databinding.LobbyFragmentBinding;
import kotlin.Unit;


@AndroidEntryPoint
public class LobbyFragment extends Fragment {

    private final static String TAG = "Lobby";

    private LobbyFragmentBinding binding;
    private LobbyViewModel mViewModel;

    private Message message;
    private MessageListener messageListener;

    public LobbyFragment() {
    }

    public static LobbyFragment newInstance() {
        return new LobbyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.lobby_fragment, container, false);

        messageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d(TAG, "Found message: " + new String(message.getContent()));
            }

            @Override
            public void onLost(Message message) {
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
            }
        };

        message = new Message("Hello World".getBytes());

        binding.lobbyExposures.setAdapter(new LobbyExposureAdapter(new ArrayList<>(), item -> {/* Do nothing... */}));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(LobbyViewModel.class);
        binding.setVm(mViewModel);

        binding.swpBtnCheckInOut.setOnSwipedOnListener(this::onSwipedOn);
        binding.swpBtnCheckInOut.setOnSwipedOffListener(this::onSwipedOff);
    }

    private Unit onSwipedOn() {
        Nearby.getMessagesClient(getActivity()).publish(message);
        Nearby.getMessagesClient(getActivity()).subscribe(messageListener);

        animateOn();
        return null;
    }

    private Unit onSwipedOff() {
        Nearby.getMessagesClient(getActivity()).unpublish(message);
        Nearby.getMessagesClient(getActivity()).unsubscribe(messageListener);

        animateOff();
        return null;
    }


    private void animateOn() {
        TransitionManager.beginDelayedTransition(binding.frameLayout6);
        binding.txtLobbyWelcome.setVisibility(View.GONE);
        binding.txtLobbyDescription.setVisibility(View.GONE);
        binding.cntLobbyContacts.setVisibility(View.VISIBLE);
    }

    private void animateOff() {
        TransitionManager.beginDelayedTransition(binding.frameLayout6);
        binding.txtLobbyWelcome.setVisibility(View.VISIBLE);
        binding.txtLobbyDescription.setVisibility(View.VISIBLE);
        binding.cntLobbyContacts.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        Nearby.getMessagesClient(getActivity()).unpublish(message);
        Nearby.getMessagesClient(getActivity()).unsubscribe(messageListener);
        super.onStop();
    }
}