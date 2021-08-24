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
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.GroupieAdapter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.items.LobbyExposureItem;
import de.uni.cc2coronotracker.data.adapters.items.LobbyHeaderItem;
import de.uni.cc2coronotracker.data.viewmodel.LobbyViewModel;
import de.uni.cc2coronotracker.databinding.LobbyFragmentBinding;
import kotlin.Unit;


/**
 * The lobby uses Google Nearby to automatically detect and add exposures with nearby contacts.
 */
@AndroidEntryPoint
public class LobbyFragment extends Fragment {

    private final static String TAG = "Lobby";

    private LobbyFragmentBinding binding;
    private LobbyViewModel mViewModel;

    /**
     * The message client used for google nearby.
     */
    private MessagesClient messageClient;
    /**
     * The message listener for this device.
     */
    private MessageListener messageListener;

    /**
     * The currently published message if any, null otherwise.
     */
    private Message currentMessage = null;


    /**
     * Allows us to only receive messages meant for us (Meaning having our Type and Namespace)
     * Set to {@link MessageFilter.Builder#includeAllMyTypes()}
     */
    private final MessageFilter messageFilter = new MessageFilter.Builder()
            .includeAllMyTypes()
            .build();

    /**
     * SubscriptionOptions used by our {@code MessageClient}
     * BLE Only, TTL_SECONDS_DEFAULT
     */
    private final SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
            .setFilter(messageFilter)
            .setStrategy(new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST).setTtlSeconds(Strategy.TTL_SECONDS_MAX).build())
            .setCallback(new SubscribeCallback() {
                @Override
                public void onExpired() {
                    super.onExpired();
                    Log.e(TAG, "Subscription expired!");
                }
            }).build();

    /**
     * PublishOptions used by our {@code MessageClient}
     * BLE Only, TTL_SECONDS_DEFAULT
     */
    private final PublishOptions publishOptions = new PublishOptions.Builder()
            .setStrategy(new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST).setTtlSeconds(Strategy.TTL_SECONDS_MAX).build())
            .setCallback(new PublishCallback() {
                @Override
                public void onExpired() {
                    super.onExpired();
                    Log.e(TAG, "Publication expired!");
                }
            })
            .build();


    private final GroupieAdapter rvAdapter = new GroupieAdapter();

    public LobbyFragment() {
    }

    /**
     * Called when the publication of a message failed.
     * @param e The failure reason
     */
    private static void onPublishFailure(Exception e) {
        Log.e(TAG, "Failed to publish message", e);
    }

    /**
     * Called, when a message is published successfully.
     * @param unused Unused
     */
    private static void onPublishSuccess(Void unused) {
        Log.d(TAG, "Publish successful.");
    }

    /**
     * Called, when subscription to new messages was successfull.
     * @param unused Unused
     */
    private static void onSubscriptionSuccess(Void unused) {
        Log.d(TAG, "Successfully subscribed to messages.");
    }

    /**
     * Called, when subscription to new messages failed
     * @param e The failure reason
     */
    private static void onSubscriptionFailure(Exception e) {
        Log.e(TAG, "Failed to subscribe to new messages.", e);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.lobby_fragment, container, false);

        // Initialize the message listener. Basicly calls the viewmodels onFound and onLost methods.
        messageListener = new MessageListener() {
            @Override
            public void onFound(@NonNull Message message) {
                mViewModel.onFound(message);
            }

            @Override
            public void onLost(@NonNull Message message) {
                mViewModel.onLost(message);
            }
        };

        binding.lobbyExposures.setAdapter(rvAdapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messageClient = Nearby.getMessagesClient(getActivity(),
                new MessagesOptions.Builder().setPermissions(NearbyPermissions.BLE).build());

        mViewModel = new ViewModelProvider(this).get(LobbyViewModel.class);
        binding.setVm(mViewModel);

        binding.swpBtnCheckInOut.setOnSwipedOnListener(this::onSwipedOn);
        binding.swpBtnCheckInOut.setOnSwipedOffListener(this::onSwipedOff);

        mViewModel.getCurrentExposures().observe(getViewLifecycleOwner(), this::onRvChanged);

        mViewModel.getCurrentMessage().observe(getViewLifecycleOwner(), this::onNewMessage);
    }

    private void onRvChanged(Map<UUID, List<LobbyViewModel.ProgressiveExposure>> exposureMap) {
        if (exposureMap == null || exposureMap.isEmpty()) {
            rvAdapter.clear();
            return;
        }

        rvAdapter.clear();
        for (Map.Entry<UUID, List<LobbyViewModel.ProgressiveExposure>> entry : exposureMap.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;

            LobbyHeaderItem lobbyHeaderItem = new LobbyHeaderItem(null);
            ExpandableGroup group = new ExpandableGroup(lobbyHeaderItem, true);
            //group.registerGroupDataObserver(rvAdapter);

            for (LobbyViewModel.ProgressiveExposure exposure : entry.getValue()) {
                group.add(new LobbyExposureItem(exposure));
            }
            rvAdapter.add(group);
        }

        animateHideNoExposures();
    }

    /**
     * Resets "no exposure" indicators visibility (image + desc)
     */
    private void animateShowNoExposures() {
        binding.lobbyNoEncounterYetDesc.setVisibility(View.VISIBLE);
        binding.lobbyNoEncounterYetLogo.setVisibility(View.VISIBLE);
    }

    /**
     * (Animates) hiding of the "no exposure" indicator (image + desc)
     * @implNote Currently does not animate, since it leads to slight flickering under certain
     * circumstances, that ruins the polished feel it is aimed to achieve.
     */
    private void animateHideNoExposures() {
        binding.lobbyNoEncounterYetDesc.setVisibility(View.GONE);
        binding.lobbyNoEncounterYetLogo.setVisibility(View.GONE);
    }

    /**
     * Called when the view model requests a new message to be published.
     * Unpublishes the old message if any, then republishes the new message.
     * @param message The new message to be published
     */
    private void onNewMessage(LobbyViewModel.LobbyMessage message) {

        // Simply unpublish?
        if (message == null) {
            unpublishCurrentMessage();
            return;
        }

        unpublishCurrentMessage();
        currentMessage = message.toMessage();

        messageClient.publish(currentMessage, publishOptions)
            .addOnSuccessListener(LobbyFragment::onPublishSuccess)
            .addOnFailureListener(LobbyFragment::onPublishFailure);
    }

    /**
     * Called, when the user swipes on to join the lobby.
     * @return null
     */
    private Unit onSwipedOn() {
        messageClient.subscribe(messageListener, subscribeOptions)
        .addOnSuccessListener(LobbyFragment::onSubscriptionSuccess)
        .addOnFailureListener(LobbyFragment::onSubscriptionFailure);
        mViewModel.startBroadcast();
        animateOn();
        return null;
    }

    /**
     * Called, when the user swipes off to leave the lobby.
     * @return null
     */
    private Unit onSwipedOff() {
        unpublishCurrentMessage();
        messageClient.unsubscribe(messageListener);
        mViewModel.finalizeExposures();
        animateOff();
        return null;
    }


    /**
     * Simply animates visibility of some ui elements.
     */
    private void animateOn() {
        TransitionManager.beginDelayedTransition(binding.frameLayout6);
        binding.txtLobbyWelcome.setVisibility(View.GONE);
        binding.txtLobbyDescription.setVisibility(View.GONE);
        binding.cntLobbyContacts.setVisibility(View.VISIBLE);
    }

    /**
     * Simply animates visibility of some ui elements.
     */
    private void animateOff() {
        TransitionManager.beginDelayedTransition(binding.frameLayout6);
        binding.txtLobbyWelcome.setVisibility(View.VISIBLE);
        binding.txtLobbyDescription.setVisibility(View.VISIBLE);
        binding.cntLobbyContacts.setVisibility(View.GONE);

        animateShowNoExposures();
    }

    /**
     * {@inheritDoc}
     * Unpublishes the current message if any, then unsubscribes from nearby.
     */
    @Override
    public void onStop() {
        unpublishCurrentMessage();
        messageClient.unsubscribe(messageListener);
        mViewModel.finalizeExposures();
        super.onStop();
    }

    /**
     * Un-publishes the current message if any.
     */
    private void unpublishCurrentMessage() {
        if (messageClient != null && currentMessage != null)
            messageClient.unpublish(currentMessage);

        currentMessage = null;
    }

}