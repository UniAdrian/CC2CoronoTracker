package de.uni.cc2coronotracker.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ContactSelectionAdapter;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import de.uni.cc2coronotracker.data.viewmodel.ContactViewModel;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactSelectionDialogViewModel;
import de.uni.cc2coronotracker.databinding.DialogContactPickerBinding;

public class SelectContactDialogFragment extends DialogFragment {

    public static String TAG = "SelectContactDialog";

    private boolean isMultiSelect = false;
    private QrIntent.Intent callerIntent = null;


    private DialogContactPickerBinding binding;
    private ContactViewModel contactsViewModel;
    private ContactSelectionDialogViewModel contactSelectionViewModel;


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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_contact_picker, null, false);

        binding = DataBindingUtil.bind(view);

        binding.setContactVM(contactsViewModel);
        binding.setLifecycleOwner(this);

        binding.rvContactList.setAdapter(new ContactSelectionAdapter(new ArrayList<>(), null));

        contactsViewModel.getAllContacts().observe(this, contacts -> {
            if (contacts == null)
                return;

            binding.rvContactList.setAdapter(new ContactSelectionAdapter(contacts, this::onSingleSelect));
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
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

    private void onSingleSelect(Contact contact) {
        ArrayList<Contact> returnArray = new ArrayList<>();
        returnArray.add(contact);

        if (!isMultiSelect) {
            contactSelectionViewModel.onContactSelection(new ContactSelectionDialogViewModel.ContactIntentTuple(returnArray, callerIntent));
            this.dismiss();
        }
    }

    private void onMultiSelect() {
        List<Contact> returnArray = ((ContactSelectionAdapter)binding.rvContactList.getAdapter()).getSelected();
        contactSelectionViewModel.onContactSelection(new ContactSelectionDialogViewModel.ContactIntentTuple(returnArray, callerIntent));
        this.dismiss();
    }
}
