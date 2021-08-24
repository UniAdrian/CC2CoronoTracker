package de.uni.cc2coronotracker.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.apache.commons.lang3.StringUtils;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.viewmodel.shared.ContactCreationDialogViewModel;
import de.uni.cc2coronotracker.databinding.DialogNewContactBinding;

/**
 * Simple dialog that allows the user to create a new user from scratch
 * Includes display name and avatar for the contact.
 */
@AndroidEntryPoint
public class NewContactDialogFragment extends DialogFragment implements TextWatcher {

    private final static String TAG = "NewContactDialogVM";

    private ContactCreationDialogViewModel contactCreationViewModel;
    private DialogNewContactBinding binding;

    private Button positiveButton = null;
    private Uri imageUri = null;

    /**
     * If non null we return the updated contact
     */
    private Contact contactToUpdate = null;

    // Used to prompt the use to select an image from the gallery.
    final ActivityResultLauncher<String[]> mGetContent = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onImagePicked);

    /**
     * Creates a new instance of the dialog.
     * Should be used to create instances of this class exclusively.
     * @return A new instance of this dialog
     */
    public static NewContactDialogFragment newInstance(@Nullable Contact contact) {
        NewContactDialogFragment newDialog = new NewContactDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("fromContact", contact);
        newDialog.setArguments(args);
        return newDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactCreationViewModel = new ViewModelProvider(this.getActivity()).get(ContactCreationDialogViewModel.class);

        Bundle args = getArguments();
        if (args.containsKey("fromContact")) {
            contactToUpdate = getArguments().getParcelable("fromContact");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_new_contact, null, false);

        binding = DataBindingUtil.bind(view);

        binding.setContactVM(contactCreationViewModel);
        binding.setLifecycleOwner(this);

        // Build the dialog and set the listeners...
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

            // If we have a contact to update, set the values and enable the confirm button
            // We have to call this here, since otherwise the dialog is not initialized completely
            // and we will have NPEs all over. :P
            if (contactToUpdate != null) {
                onImagePicked(contactToUpdate.photoUri);
                binding.txtLoDisplayName.setText(contactToUpdate.displayName);
                positiveButton.setEnabled(true);
                positiveButton.setText(R.string.confirm_update_contact);
            }
        });

        // Enable the confirm button if a valid display name is entered.
        binding.txtLoDisplayName.addTextChangedListener(this);
        binding.btnSelectFromGallery.setOnClickListener(v -> this.mGetContent.launch(new String[] {"image/*"}));


        return dialog;
    }

    /**
     * Sets or resets the contact avatar.
     * Called once the activity launched by <ref>mGetContent</ref> returns.
     * @param imgUri The images url or <code>Null</code>
     */
    private void onImagePicked(Uri imgUri) {
        this.imageUri = imgUri;

        if (imgUri == null) {
            binding.imgAvatar.setImageResource(R.drawable.ic_no_avatar_128);
            binding.imgAvatar.setColorFilter(ContextCompat.getColor(getContext(), R.color.primaryTextColor), PorterDuff.Mode.SRC_IN);
            return;
        }

        try {
            getActivity().getContentResolver().takePersistableUriPermission(imgUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            binding.imgAvatar.setImageURI(imgUri);
            binding.imgAvatar.setColorFilter(null);
            binding.imgAvatar.setImageTintList(null);
        } catch (Exception e) {
            imageUri = null;
            binding.imgAvatar.setImageResource(R.drawable.ic_no_avatar_128);
            binding.imgAvatar.setColorFilter(ContextCompat.getColor(getContext(), R.color.primaryTextColor), PorterDuff.Mode.SRC_IN);
            Toast.makeText(getContext(), R.string.persist_uri_failed, Toast.LENGTH_LONG);
        }
    }

    /**
     * Called when the user actually confirms the dialog by pressing the positive action button
     * Informs potential listeners via the contactCreationViewModel
     * @param dialogInterface The dialog interface
     * @param i The button index
     */
    private void onConfirm(DialogInterface dialogInterface, int i) {
        try {
            Contact c = createContact();
            Log.d(TAG, "Posting new/updated contact: " + c);
            contactCreationViewModel.publish(c);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    /**
     * Simply creates a new contact with the current settings
     * @return The created contact.
     */
    private Contact createContact() {
        Contact c = new Contact();

        if (contactToUpdate != null) {
            c = contactToUpdate;
        }

        c.displayName = binding.txtLoDisplayName.getText().toString();
        c.photoUri = this.imageUri;

        return c;
    }


    /**
     * Called when the user changes the display name.
     * If the display name is null, emtpty or whitespace only the positive action button will be
     * disabled
     * @param s The current content
     * @param start Where changes begin
     * @param before The old length of the changed string
     * @param count How many characters have changed from start
     */
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
