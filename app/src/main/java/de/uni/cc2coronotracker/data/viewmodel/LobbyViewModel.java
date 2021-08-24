package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.nearby.messages.Message;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.ReadOnlySettingsProvider;

@HiltViewModel
public class LobbyViewModel extends ViewModel {

    private static final String TAG = "LobbyViewModel";

    /**
     * Resume threshold for progressive exposures in ms.
     * Basicly, if the message is re-found again before the threshold is passed, the exposure is
     * instead re-opened instead of adding a new one.
     */
    private static final long RESUME_THRESHOLD = 25000;

    private final ReadOnlySettingsProvider settingsProvider;
    private final ContactRepository contactRepository;

    private final MutableLiveData<Map<UUID, List<ProgressiveExposure>>> currentExposures = new MutableLiveData<>();
    private final MutableLiveData<LobbyMessage> currentMessage = new MutableLiveData<>();

    /**
     * Used for fast retrieval of contacts for received UUIDS
     * Also acts as a lookup-cache.
     */
    private final Map<UUID, Contact> contactCache = new HashMap<>();


    @Inject
    public LobbyViewModel(ReadOnlySettingsProvider settingsProvider, ContactRepository contactRepository) {
        this.settingsProvider = settingsProvider;
        this.contactRepository = contactRepository;
    }

    public void startBroadcast() {
        UUID uuid = settingsProvider.getPersonalUUID();
        currentMessage.postValue(new LobbyMessage(uuid));
    }

    /**
     * Called, when a message is detected.
     * Adds new exposures or - if present and the {@link #RESUME_THRESHOLD} has not yet passed -
     * resumes the previously added exposure
     * @param message The new message found
     */
    public void onFound(Message message) {
        Log.d(TAG, "Found message: " + message);

        try {
            LobbyMessage lobbyMessage = LobbyMessage.fromByteArray(message.getContent());

            CacheContact(lobbyMessage.uuid);

            ProgressiveExposure newExposure = new ProgressiveExposure();
            newExposure.uuid = lobbyMessage.uuid;
            newExposure.start = new Date();

            Map<UUID, List<ProgressiveExposure>> exposureMap = currentExposures.getValue();
            if (exposureMap == null) {
                exposureMap = new HashMap<>();
            }

            List<ProgressiveExposure> exposuresList = exposureMap.get(lobbyMessage.uuid);
            if (exposuresList == null) {
                exposuresList = new ArrayList<>();
            }

            // Check wether or not we can "resume" the last exposure.
            if (!exposuresList.isEmpty()) {
                int lastIndex = exposuresList.size() - 1;
                ProgressiveExposure lastExposure = exposuresList.get(lastIndex);
                if (lastExposure.end.getTime() + RESUME_THRESHOLD >= new Date().getTime()) {
                    // We can resume the last exposure instead of adding a new one. :)
                    lastExposure.end = null;
                    exposuresList.set(lastIndex, lastExposure);
                } else {
                    exposuresList.add(newExposure);
                }
            } else {
                exposuresList.add(newExposure);
            }

            // Update our state
            exposureMap.put(lobbyMessage.uuid, exposuresList);
            currentExposures.postValue(exposureMap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to process message.", e);
        }
    }

    /**
     * Tries to fetch and cache a contact for the uuid if not yet present.
     * @param uuid The uuid to fetch
     */
    private void CacheContact(UUID uuid) {
        // Already cached?
        if (contactCache.containsKey(uuid)) {
            return;
        }

        contactRepository.getContact(uuid, result -> {
            if (result instanceof Result.Success) {
                contactCache.put(uuid, ((Result.Success<Contact>) result).data);
            } else if (result instanceof Result.Error) {
                Log.e(TAG, "Failed to fetch contact for UUID: " + uuid, ((Result.Error<Contact>) result).exception);
            }
        });
    }

    /**
     * Called when a message is lost.
     * @param message The message that cannot be found anymore
     */
    public void onLost(Message message) {
        Log.d(TAG, "Lost message: " + message);

        try {
            LobbyMessage lobbyMessage = LobbyMessage.fromByteArray(message.getContent());

            // All objects handled here must be non-null at this point. So we do not bother checking.
            // Every error that might occur is otherwise caught in the encompassing try-catch
            Map<UUID, List<ProgressiveExposure>> exposureMap = currentExposures.getValue();
            List<ProgressiveExposure> exposureList = exposureMap.get(lobbyMessage.uuid);

            // Update the list
            int idx = exposureList.size()-1;
            ProgressiveExposure currentExposure = exposureList.get(idx);
            currentExposure.end = new Date();
            exposureList.set(idx, currentExposure);

            // Update our state.
            exposureMap.put(lobbyMessage.uuid, exposureList);
            currentExposures.postValue(exposureMap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to process message.", e);
        }
    }

    /**
     * Fianlizes all current exposures as best as possible.
     */
    public void finalizeExposures() {

        // Reset state
        currentExposures.postValue(null);
        currentMessage.postValue(null);
        // We also clear the cache to save some memory. ;)
        contactCache.clear();
    }


    public LiveData<Map<UUID, List<ProgressiveExposure>>> getCurrentExposures() {
        return currentExposures;
    }

    public LiveData<LobbyMessage> getCurrentMessage() {
        return currentMessage;
    }


    public static class ProgressiveExposure {
        public Date start;
        public Date end;
        public UUID uuid;
        public Contact contact;
    }

    public static class LobbyMessage implements Serializable {

        /**
         * Future proofing.
         * Allows us to add more functionality without changing the underlying message set
         */
        public enum LOBBY_MESSAGE_FLAGS {
            LOBBY_MESSAGE_NONE(0),
            LOBBY_MESSAGE_ALLOW_TRACKING(1 << 1);


            private int ord;
            LOBBY_MESSAGE_FLAGS(int ord) {
                this.ord = ord;
            }
        }

        private final UUID uuid;
        private int flags = 0;

        public LobbyMessage(UUID uuid) {
            this.uuid = uuid;
            this.flags = LOBBY_MESSAGE_FLAGS.LOBBY_MESSAGE_NONE.ord;
        }

        public int getFlags() {return flags;}
        public void setFlags(int newFlags) {flags=newFlags;}

        public @NonNull byte[] toByteArray() {
            return SerializationUtils.serialize(this);
        }

        public @NonNull Message toMessage() {
            return new Message(toByteArray(),"ident");
        }

        public static LobbyMessage fromByteArray(@NonNull byte[] raw) {
            return SerializationUtils.deserialize(raw);
        }

        @Override
        public String toString() {
            return "LobbyMessage{" +
                    "uuid=" + uuid +
                    ", flags=" + flags +
                    '}';
        }
    }
}