package de.uni.cc2coronotracker.data.viewmodel.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.helper.Event;


/**
 * Used to mediate between the NewContactDialogFragment/-ViewModel and other components.
 */
@HiltViewModel
public class ContactCreationDialogViewModel extends ViewModel {
    private final MutableLiveData<Event<Contact>> newContacts = new MutableLiveData<>();

    @Inject
    public ContactCreationDialogViewModel() {
    }

    public LiveData<Event<Contact>> getNewContacts() {
        return newContacts;
    }

    public void publish(Contact c) {
        newContacts.postValue(new Event<>(c));
    }
}
