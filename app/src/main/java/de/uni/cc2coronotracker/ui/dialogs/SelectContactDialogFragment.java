package de.uni.cc2coronotracker.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ContactSelectionAdapter;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.viewmodel.ContactViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
import de.uni.cc2coronotracker.databinding.DialogContactPickerBinding;

/**
 * Allows the user to select a contact from a list of all contacts.
 * Allows single and multi select mode by passing the argument {@code isMultiSelect} to the dialog.
 * Also optionally forwards an intent associated with the dialog via the {@code intent} parameter
 * on confirmation.
 */
public class SelectContactDialogFragment extends DialogFragment {

    private boolean isMultiSelect = false;
    private QrIntent.Intent callerIntent = null;


    private DialogContactPickerBinding binding;
    private ContactViewModel contactsViewModel;

    private ContactSelectionDialogViewModel contactSelectionViewModel;


    /**
     * Creates a new instance of the DialogFragment
     * Should be used exclusively to create an instance of this class.
     * @param multiSelect If true allows selection of multiple contact, single contact otherwise
     * @param intent Optional intent associated with this dialog.
     * @return
     */
    public static SelectContactDialogFragment newInstance(boolean multiSelect, @Nullable QrIntent.Intent intent) {
        SelectContactDialogFragment fragmentDemo = new SelectContactDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMultiSelect", multiSelect);
        args.putSerializable("intent", intent);
        fragmentDemo.setArguments(args);
        return fragmentDemo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            isMultiSelect = arguments.getBoolean("isMultiSelect");
            callerIntent = (QrIntent.Intent) arguments.getSerializable("intent");
        }
        contactsViewModel = new ViewModelProvider(this.getActivity()).get(ContactViewModel.class);
        contactSelectionViewModel = new ViewModelProvider(this.getActivity()).get(ContactSelectionDialogViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_contact_picker, null, false);

        binding = DataBindingUtil.bind(view);

        binding.setContactVM(contactsViewModel);
        binding.setLifecycleOwner(this);

        binding.rvContactList.setAdapter(new ContactSelectionAdapter(new ArrayList<>(), null));

        contactsViewModel.getAllContactsWithExposures().observe(this, contactsWExposures -> {
            if (contactsWExposures == null)
                return;

            // Streams would unfortunately require a higher API level, so we stick with traditional loops here.
            List<Contact> contactList = new ArrayList<>(contactsWExposures.size());
            for (ContactDao.ContactWithExposures cwe : contactsWExposures) {
                contactList.add(cwe.contact);
            }
            binding.rvContactList.setAdapter(new ContactSelectionAdapter(contactList, this::onSingleSelect));
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        builder.setView(view)
                .setNegativeButton(R.string.dialog_cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .setTitle(R.string.pick_contact);

        if (isMultiSelect) {
            builder.setPositiveButton(R.string.dialog_select, (dialogInterface, i) -> {
                onMultiSelect();
                dialogInterface.dismiss();
            }).setTitle(R.string.pick_contacts);
        }

        return builder.create();
    }

    /**
     * Called when a contact is clicked in Single select mode
     * @param contact
     */
    private void onSingleSelect(Contact contact) {
        ArrayList<Contact> returnArray = new ArrayList<>();
        returnArray.add(contact);

        if (!isMultiSelect) {
            contactSelectionViewModel.onContactSelection(new ContactSelectionDialogViewModel.ContactIntentTuple(returnArray, callerIntent));
            this.dismiss();
        }
    }

    /**
     * Called when the user confirms selection in multi select mode.
     */
    private void onMultiSelect() {
        List<Contact> returnArray = ((ContactSelectionAdapter)binding.rvContactList.getAdapter()).getSelected();
        contactSelectionViewModel.onContactSelection(new ContactSelectionDialogViewModel.ContactIntentTuple(returnArray, callerIntent));
        this.dismiss();
    }
}
