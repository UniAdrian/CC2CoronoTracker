package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ExposureAdapter;
import de.uni.cc2coronotracker.data.viewmodel.ContactDetailsViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.AddExposureSharedViewModel;
import de.uni.cc2coronotracker.databinding.FragmentContactDetailsBinding;

@AndroidEntryPoint
public class ContactDetailsFragment extends Fragment {

    private FragmentContactDetailsBinding binding;

    private ContactDetailsViewModel contactsDetailsViewModel;
    private AddExposureSharedViewModel addExposureViewModel;

    public ContactDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactsDetailsViewModel = new ViewModelProvider(this).get(ContactDetailsViewModel.class);
        addExposureViewModel = new ViewModelProvider(this.getActivity()).get(AddExposureSharedViewModel.class);

        contactsDetailsViewModel.getExposures().observe(this, exposures -> {
            if (exposures == null) {
                exposures = new ArrayList<>();
            }
            binding.contactDetailsExposures.setAdapter(new ExposureAdapter(exposures));
        });

        contactsDetailsViewModel.getContact().observe(this, contact -> {
            if (contact.photoUri != null) {
                binding.contactAvatar.setImageURI(contact.photoUri);
            } else {
                binding.contactAvatar.setImageResource(R.drawable.ic_no_avatar_128);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_details, container, false);

        binding.setDetailsVM(contactsDetailsViewModel);
        binding.setLifecycleOwner(this);

        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ContactDetailsFragmentArgs contactDetailsFragmentArgs = ContactDetailsFragmentArgs.fromBundle(getArguments());
        if (contactDetailsFragmentArgs == null) {
            Log.e("Details", "No saved instance state... :|");
        } else {
            contactsDetailsViewModel.setContactId(contactDetailsFragmentArgs.getContactId());
        }
    }
}