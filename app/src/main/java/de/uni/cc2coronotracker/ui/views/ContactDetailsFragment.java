package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ExposureAdapter;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.viewmodel.ContactDetailsViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactCreationDialogViewModel;
import de.uni.cc2coronotracker.databinding.FragmentContactDetailsBinding;

@AndroidEntryPoint
public class ContactDetailsFragment extends Fragment {

    private final String TAG = "ContactDetailsFragment";

    private FragmentContactDetailsBinding binding;
    private ContactDetailsViewModel contactsDetailsViewModel;
    private ContactCreationDialogViewModel contactCreationViewModel;

    public ContactDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactsDetailsViewModel = new ViewModelProvider(this).get(ContactDetailsViewModel.class);
        // Use getActivity here, so we actually share the vm.
        contactCreationViewModel = new ViewModelProvider(getActivity()).get(ContactCreationDialogViewModel.class);

        contactsDetailsViewModel.getExposures().observe(this, exposures -> {
            Log.d("EXPOSURES", "Exposures: " + exposures);
            if (exposures == null) {
                exposures = new ArrayList<>();
            }
            binding.contactDetailsExposures.setAdapter(new ExposureAdapter(exposures, getContext()));
        });

        contactsDetailsViewModel.getContact().observe(this, contact -> {
            if (contact == null)
                return;

            if (contact.photoUri != null) {
                binding.contactAvatar.setImageURI(contact.photoUri);
            } else {
                binding.contactAvatar.setImageResource(R.drawable.ic_no_avatar_128);
            }

            Toolbar toolbar = requireActivity().findViewById(R.id.app_toolbar_top);
            toolbar.setTitle(contact.displayName);
        });

        // Handle contact updates etc.
        contactCreationViewModel.getNewContacts().observe(this, event -> {
            Log.d(TAG, "Got contact from contactCreationViewModel: " + event.peekContent());
            // If the contact has an invalid id we return, its probably a new contact.
            if (event.peekContent() == null || event.peekContent().id < 1) return;

            Contact clm = event.getContentIfNotHandled();
            if (clm != null) {
                contactsDetailsViewModel.updateContact(clm);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_details, container, false);

        binding.setDetailsVM(contactsDetailsViewModel);
        binding.setLifecycleOwner(this);

        binding.btnEditContact.setOnClickListener((v) -> contactsDetailsViewModel.openEditContactDialog());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ContactDetailsFragmentArgs contactDetailsFragmentArgs = ContactDetailsFragmentArgs.fromBundle(getArguments());
        if (contactDetailsFragmentArgs == null) {
            Log.e(TAG, "No saved instance state... :|");
        } else {
            contactsDetailsViewModel.setContactId(contactDetailsFragmentArgs.getContactId());
        }
    }
}