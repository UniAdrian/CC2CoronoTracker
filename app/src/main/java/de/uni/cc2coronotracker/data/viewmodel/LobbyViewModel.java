package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.nearby.messages.Message;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.repositories.providers.ReadOnlySettingsProvider;

@HiltViewModel
public class LobbyViewModel extends ViewModel {

    private static final String TAG = "LobbyViewModel";

    private final ReadOnlySettingsProvider settingsProvider;

    private final MutableLiveData<List<ProgressiveExposure>> currentExposures = new MutableLiveData<>();
    private final MutableLiveData<LobbyMessage> currentMessage = new MutableLiveData<>();

    @Inject
    public LobbyViewModel(ReadOnlySettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public void startBroadcast() {
        UUID uuid = settingsProvider.getPersonalUUID();
        currentMessage.postValue(new LobbyMessage(uuid));
    }

    /**
     * Called, when a message is detected.
     * @param message The new message found
     */
    public void onFound(Message message) {
        Log.d(TAG, "Found message: " + message);
    }

    /**
     * Called when a message is lost.
     * @param message The message that cannot be found anymore
     */
    public void onLost(Message message) {
        Log.d(TAG, "Lost message: " + message);
    }

    /**
     * Fianlizes all current exposures as best as possible.
     */
    public void finalizeExposures() {
    }


    public LiveData<List<ProgressiveExposure>> getCurrentExposures() {
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

        private final UUID uuid;
        private int flags = 0;

        public LobbyMessage(UUID uuid) {
            this.uuid = uuid;
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