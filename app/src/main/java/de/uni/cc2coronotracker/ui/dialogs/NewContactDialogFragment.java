package de.uni.cc2coronotracker.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.apache.commons.lang3.StringUtils;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactCreationDialogViewModel;
import de.uni.cc2coronotracker.databinding.DialogNewContactBinding;

@AndroidEntryPoint
public class NewContactDialogFragment extends DialogFragment implements TextWatcher {

    private ContactCreationDialogViewModel contactCreationViewModel;
    private DialogNewContactBinding binding;

    private Button positiveButton = null;

    private Uri imageUri = null;

    ActivityResultLauncher<String[]> mGetContent = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onImagePicked);

    public static NewContactDialogFragment newInstance() {
        NewContactDialogFragment newDialog = new NewContactDialogFragment();

        Bundle args = new Bundle();
        // Add args as needed.

        newDialog.setArguments(args);
        return newDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactCreationViewModel = new ViewModelProvider(this.getActivity()).get(ContactCreationDialogViewModel.class);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_new_contact, null, false);

        binding = DataBindingUtil.bind(view);

        binding.setContactVM(contactCreationViewModel);
        binding.setLifecycleOwner(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        builder.setView(view)
                .setPositiveButton(R.string.confirm_create_contact, this::onConfirm)
                .setNegativeButton(R.string.dialog_cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .setTitle(R.string.create_contact);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            // initially disable the confirm button.
            positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);
        });


        // Enable the confirm button if a valid display name is entered.
        binding.txtLoDisplayName.addTextChangedListener(this);

        binding.btnSelectFromGallery.setOnClickListener(v -> {
            this.mGetContent.launch(new String[] {"image/*"});
        });

        return dialog;
    }

    // Called once the intent returns.
    private void onImagePicked(Uri imgUri) {
        this.imageUri = imgUri;

        if (imgUri == null) {
            binding.imgAvatar.setImageResource(R.drawable.ic_no_avatar_128);
            return;
        }

        try {
            getActivity().getContentResolver().takePersistableUriPermission(imgUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            binding.imgAvatar.setImageURI(imgUri);
        } catch (Exception e) {
            imageUri = null;
            binding.imgAvatar.setImageResource(R.drawable.ic_no_avatar_128);
            Toast.makeText(getContext(), R.string.persist_uri_failed, Toast.LENGTH_LONG);
        }
    }


    private void onConfirm(DialogInterface dialogInterface, int i) {
        try {
            Contact c = createContact();
            contactCreationViewModel.publish(c);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Contact createContact() {
        Contact c = new Contact();

        c.displayName = binding.txtLoDisplayName.getText().toString();
        c.photoUri = this.imageUri;

        return c;
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        positiveButton.setEnabled(false);
        String displayName = binding.txtLoDisplayName.getText().toString();

        // Validate the display name
        if (StringUtils.isBlank(displayName)) {
            String mustNotBeBlank = getResources().getString(R.string.display_name_cannnot_be_blank);
            binding.txtLoDisplayName.setError(mustNotBeBlank);
        } else {
            binding.txtLoDisplayName.setError(null);
            positiveButton.setEnabled(true);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void afterTextChanged(Editable s) {
    }

}
