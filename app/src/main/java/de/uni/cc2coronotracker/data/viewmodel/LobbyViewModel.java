package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.nearby.messages.Message;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.ReadOnlySettingsProvider;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.RequestFactory;

@HiltViewModel
public class LobbyViewModel extends ViewModel {

    private static final String TAG = "LobbyViewModel";


    /**
     * Resume threshold for progressive exposures in ms.
     * Basicly, if the message is re-found again before the threshold is passed, the exposure is
     * instead re-opened instead of adding a new one.
     */
    private static final long RESUME_THRESHOLD = 25000;

    /**
     * How often the {@link #dbUpdateTimer} is supposed to check for changes and update the db
     */
    private static final long DB_UPDATE_INTERVAL = 500;


    private final ReadOnlySettingsProvider settingsProvider;
    private final ContactRepository contactRepository;
    private final ExposureRepository exposureRepository;

    private final ContextMediator ctxMediator;

    /**
     * Holds the current exposure map or null if not active.
     * @implNote The map stored here is a ConcurrentHashMap, since it may be updated in parallel due to
     * the {@link #dbUpdateTimer}
     */
    private final MutableLiveData<Map<UUID, List<ExposureContactPair>>> currentExposures = new MutableLiveData<>();

    /**
     * Current message published or null if not active.
     */
    private final MutableLiveData<LobbyMessage> currentMessage = new MutableLiveData<>();


    /**
     * While broadcasting/receiving messages checks the {@link #currentExposures} map every {@link #DB_UPDATE_INTERVAL}ms
     */
    private Timer dbUpdateTimer = null;

    /**
     * Used for fast retrieval of contacts for received UUIDS
     * Also acts as a lookup-cache.
     */
    private final Map<UUID, Contact> contactCache = new HashMap<>();

    @Inject
    public LobbyViewModel(ReadOnlySettingsProvider settingsProvider, ContactRepository contactRepository, ExposureRepository exposureRepository, ContextMediator ctxMediator) {
        this.settingsProvider = settingsProvider;
        this.contactRepository = contactRepository;
        this.exposureRepository = exposureRepository;
        this.ctxMediator = ctxMediator;
    }

    /**
     * Fetches the personal UUID and requests publish of the corresponding LobbyMessage.
     * Also starts the background service to
     */
    public void startBroadcast() {
        UUID uuid = settingsProvider.getPersonalUUID();
        currentMessage.postValue(new LobbyMessage(uuid));

        dbUpdateTimer = new Timer();
        dbUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDatabase();
            }
        }, DB_UPDATE_INTERVAL, DB_UPDATE_INTERVAL);
    }


    /**
     * Called by {@link #dbUpdateTimer} to update/insert exposures into the database.
     * @implNote Only inserts Exposures with a valid {@link Exposure#endDate} to prevent
     * dangling exposures if the application is killed unexpectedly or the lobby is otherwise closed
     * unexpectedly. Ongoing exposures are inserted/updated as if they would end at execution time
     * and kept up to date in later runs as well.
     */
    private void updateDatabase() {
        if (currentExposures.getValue() == null || currentExposures.getValue().isEmpty()) {
            return;
        }

        // Copy the current state
        final var toUpdate = currentExposures.getValue();

        for (var entries : toUpdate.entrySet()) {
            UUID uuid = entries.getKey();
            List<ExposureContactPair> values = entries.getValue();

            if (values == null || values.isEmpty())
                continue;

            int idx = values.size()-1;
            var currentEntry = values.get(idx);

            // Do we need to work? That is: isDirty is true or endDate is null?
            if (currentEntry.exposure.endDate != null && !currentEntry.isDirty) {
                continue;
            }

            Log.d(TAG, "- Needs update (Ongoing: " + !currentEntry.isDirty + "): " + uuid);
            // Insert new exposure or update existing?
            if (currentEntry.exposure.id < 1) {
                insertNewExposure(idx, uuid, currentEntry.exposure);
            } else {
                updateExposure(currentEntry.exposure, uuid, idx);
            }
        }
    }

    /**
     * Updates an exposure in the database to the new values.
     * If an exposure is re-opened acts as if the exposure ended at call-time.
     * @param exposure The exposure to update.
     * @param uuid The uuid of the corresponding contact
     * @param idx The index in the current map
     */
    private void updateExposure(Exposure exposure, UUID uuid, int idx) {
        Exposure toUpdate = new Exposure(exposure);
        if (toUpdate.endDate == null) {
            toUpdate.endDate = new java.sql.Date(new Date().getTime());
        }

        exposureRepository.updateExposure(toUpdate, result -> {
            if (result instanceof Result.Success) {
                Log.d(TAG, "-- Updated exposure: " + exposure);
                Map<UUID, List<ExposureContactPair>> currentMap = currentExposures.getValue();
                if (currentMap == null || currentMap.isEmpty()) {
                    return;
                }

                if (currentMap.containsKey(uuid)) {
                    List<ExposureContactPair> currentList = currentMap.get(uuid);
                    if (currentList == null || currentList.size() <= idx || currentList.get(idx).exposure == null) {
                        return;
                    }

                    // Are we still working on the same exposure?
                    Exposure toCompare = currentList.get(idx).exposure;
                    if (toCompare.contactId != exposure.contactId || toCompare.startDate != exposure.startDate) {
                        return;
                    }

                    // Did the exposure change in the meantime?
                    if (toCompare.endDate != toUpdate.endDate) {
                        return;
                    }

                    // Okay, we can remove the flag. ^^"
                    currentList.get(idx).isDirty = false;
                }
            } else if (result instanceof Result.Error) {
                Log.e(TAG, "-- Failed to update exposure, ignoring until next run.", ((Result.Error<Void>) result).exception);
            }
        });
    }

    /**
     * Inserts a new exposure in the database. If it is an ongoing exposure, it is inserted as if it
     * ended at call-time.
     * If the old data is still active once inserted, updates the current map with the new id.
     * @param idx The index in the current map
     * @param uuid The uuid of the contact
     * @param exposure The exposure to insert
     */
    private void insertNewExposure(int idx, UUID uuid, Exposure exposure) {
        // Set the end date for insertion if ongoing.
        Exposure toInsert = new Exposure(exposure);
        if (toInsert.endDate == null) {
            toInsert.endDate = new java.sql.Date(new Date().getTime());
        }

        // Since it is not necessarily guaranteed, that the lobby is still active we have to check
        // every step of the insertion.
        exposureRepository.addExposure(toInsert, result -> {
            if (result instanceof Result.Success) {
                Map<UUID, List<ExposureContactPair>> currentMap = currentExposures.getValue();
                if (currentMap == null || currentMap.isEmpty()) {
                    return;
                }

                if (currentMap.containsKey(uuid)) {
                    List<ExposureContactPair> currentList = currentMap.get(uuid);
                    if (currentList == null || currentList.size() <= idx || currentList.get(idx).exposure == null) {
                        return;
                    }

                    // Are we still working on the same exposure?
                    Exposure toCompare = currentList.get(idx).exposure;
                    if (toCompare.contactId != exposure.contactId || toCompare.startDate != exposure.startDate) {
                        return;
                    }

                    currentList.get(idx).isDirty = false;
                    currentList.get(idx).exposure.id = ((Result.Success<Long>) result).data;
                    Log.d(TAG, "-- Inserted new exposure: " + currentList.get(idx).exposure);
                }
            } else if(result instanceof  Result.Error) {
                Log.e(TAG, "-- Failed to insert new exposure, skipping till next run.", ((Result.Error<Long>) result).exception);
            }
        });
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

            if (contactCache.containsKey(lobbyMessage.uuid)) {
                processMessage(lobbyMessage, contactCache.get(lobbyMessage.uuid));
                return;
            }

            // TODO: As soon as there is time implement retry strategies for the error cases.
            // (I recommend the strategy pattern for different retry-strategies)
            contactRepository.getContact(lobbyMessage.uuid, result -> {
                if (result instanceof Result.Success) {
                    Contact fetchedContact = ((Result.Success<Contact>) result).data;
                    if (fetchedContact != null) {
                        contactCache.put(lobbyMessage.uuid, ((Result.Success<Contact>) result).data);
                        processMessage(lobbyMessage, contactCache.get(lobbyMessage.uuid));
                        return;
                    }
                    // Insert the contact, then proceed.
                    Contact newContact = new Contact();
                    newContact.uuid = lobbyMessage.uuid;
                    newContact.displayName = lobbyMessage.uuid.toString();

                    contactRepository.insertContact(newContact, (insertionResult) -> {
                        if (insertionResult instanceof Result.Success) {
                            processMessage(lobbyMessage, ((Result.Success<Contact>) result).data);
                        } else if (insertionResult instanceof Result.Error) {
                            Log.e(TAG, "Failed to process message for UUID: " + lobbyMessage.uuid, ((Result.Error<Long>) insertionResult).exception);
                            ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.lobby_insert_contact_failed, Snackbar.LENGTH_SHORT));
                        }
                    });
                } else if (result instanceof Result.Error) {
                    Log.e(TAG, "Failed to fetch contact for UUID: " + lobbyMessage.uuid, ((Result.Error<Contact>) result).exception);
                    ctxMediator.request(RequestFactory.createSnackbarRequest(R.string.lobby_contact_fetch_failed, Snackbar.LENGTH_SHORT));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to process message.", e);
        }
    }

    /**
     * Adds new exposures or - if present and the {@link #RESUME_THRESHOLD} has not yet passed -
     * resumes the previously added exposure
     * Called by {@link #onFound(Message)}
     * @param lobbyMessage The parsed lobbyMessage received via {@link #onFound(Message)}
     * @param contact The contact associated with the messages uuid
     */
    private void processMessage(LobbyMessage lobbyMessage, Contact contact) {
        ExposureContactPair newExposurePair = new ExposureContactPair();
        Exposure newExposure = new Exposure();
        newExposure.contactId = contact.id;
        newExposure.startDate = new java.sql.Date(new Date().getTime());

        newExposurePair.exposure = newExposure;
        newExposurePair.contact = contact;
        newExposurePair.isDirty = true;

        Map<UUID, List<ExposureContactPair>> exposureMap = currentExposures.getValue();
        if (exposureMap == null) {
            exposureMap = new ConcurrentHashMap<>();
        }

        List<ExposureContactPair> exposuresList = exposureMap.get(lobbyMessage.uuid);
        if (exposuresList == null) {
            exposuresList = new ArrayList<>();
        }

        // Check wether or not we can "resume" the last exposure.
        if (!exposuresList.isEmpty()) {
            int lastIndex = exposuresList.size() - 1;
            ExposureContactPair lastExposure = exposuresList.get(lastIndex);
            if (lastExposure.exposure.endDate != null && lastExposure.exposure.endDate.getTime() + RESUME_THRESHOLD >= new Date().getTime()) {
                // We can resume the last exposure instead of adding a new one. :)
                lastExposure.exposure.endDate = null;
                lastExposure.isDirty = true;
                exposuresList.set(lastIndex, lastExposure);
            } else {
                exposuresList.add(newExposurePair);
            }
        } else {
            exposuresList.add(newExposurePair);
        }

        // Update our state
        exposureMap.put(lobbyMessage.uuid, exposuresList);
        currentExposures.postValue(exposureMap);
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
            Map<UUID, List<ExposureContactPair>> exposureMap = currentExposures.getValue();
            List<ExposureContactPair> exposureList = exposureMap.get(lobbyMessage.uuid);

            // Update the list
            int idx = exposureList.size()-1;
            ExposureContactPair currentExposure = exposureList.get(idx);
            currentExposure.exposure.endDate = new java.sql.Date(new Date().getTime());
            currentExposure.isDirty = true;
            exposureList.set(idx, currentExposure);

            // Update our state.
            exposureMap.put(lobbyMessage.uuid, exposureList);
            currentExposures.postValue(exposureMap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to process message.", e);
        }
    }

    /**
     * Finalizes all open exposures, settings their end date and adding them to the database.
     */
    public void finalizeExposures() {
        // Reset state
        currentExposures.postValue(null);
        currentMessage.postValue(null);

        // We also clear the cache to save some memory. ;)
        contactCache.clear();

        if (dbUpdateTimer != null) {
            dbUpdateTimer.cancel();
            dbUpdateTimer = null;
        }
    }


    public LiveData<Map<UUID, List<ExposureContactPair>>> getCurrentExposures() {
        return currentExposures;
    }

    public LiveData<LobbyMessage> getCurrentMessage() {
        return currentMessage;
    }


    /**
     * A pair of {@link Contact} and {@link Exposure} to keep track of
     * currently ongoing exposures.
     */
    public static class ExposureContactPair {
        public Exposure exposure;
        public Contact contact;
        public boolean isDirty;
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