package de.uni.cc2coronotracker.data.viewmodel.shared;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.helper.Event;


/**
 * Simple intermediate VM to communicate between a ContactSelectionDialogFragment and other ViewModels
 * depending on ContactSelectionDialogFragment's contact selection
 */
@HiltViewModel
public class ContactSelectionDialogViewModel extends ViewModel {

    final MutableLiveData<Event<ContactIntentTuple>> onContactSelection = new MutableLiveData<>();

    @Inject
    public ContactSelectionDialogViewModel() {
    }

    public LiveData<Event<ContactIntentTuple>> getOnContactSelection() {
        return onContactSelection;
    }

    public void onContactSelection(@NonNull ContactIntentTuple contactWithIntent) {
        onContactSelection.postValue(new Event<>(contactWithIntent));
    }

    public static class ContactIntentTuple {
        public final List<Contact> contactList;
        public final QrIntent.Intent intent;

        public ContactIntentTuple(List<Contact> contactList, QrIntent.Intent intent) {
            this.contactList = contactList;
            this.intent = intent;
        }
    }
}
