package de.uni.cc2coronotracker.data.adapters;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;

public class ContactSelectionAdapter extends RecyclerView.Adapter<ContactSelectionAdapter.ViewHolder> {

    public final String TAG = "ContactSelectionAdapter";

    public interface OnItemClickListener {
        void onItemClick(Contact item);
    }

    private final List<Contact> contacts;
    private final List<Contact> selected;
    private final OnItemClickListener clickListener;

    private Drawable defaultAvatar = null;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView displayNameView;
        private final ImageView avatarView;

        public ViewHolder(View view) {
            super(view);
            displayNameView = view.findViewById(R.id.crvDisplayName);
            avatarView = view.findViewById(R.id.crvAvatar);
        }

        public TextView getDisplayNameView() {
            return displayNameView;
        }
        public ImageView getAvatarView() {
            return avatarView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet List<Contact> containing the data to populate views to be used
     * by RecyclerView.
     */
    public ContactSelectionAdapter(List<Contact> dataSet, OnItemClickListener listener) {
        contacts = dataSet;
        selected = new ArrayList<>();
        clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_select_rv_item, viewGroup, false);

        if (defaultAvatar == null) {
            defaultAvatar = ResourcesCompat.getDrawable(viewGroup.getContext().getResources(), R.drawable.ic_no_avatar_128, viewGroup.getContext().getTheme());
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Contact contact = contacts.get(position);
        viewHolder.getDisplayNameView().setText(contact.displayName);

        if (contact.photoUri != null) {
            try {
                viewHolder.getAvatarView().setImageURI(contact.photoUri);
                viewHolder.getAvatarView().setColorFilter(null);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set image uri: ", e);
                viewHolder.getAvatarView().setImageDrawable(defaultAvatar);
                viewHolder.getAvatarView().setColorFilter(ContextCompat.getColor(viewHolder.avatarView.getContext(), R.color.secondaryTextColor), PorterDuff.Mode.SRC_IN);
            }
        } else {
            viewHolder.getAvatarView().setImageDrawable(defaultAvatar);
            viewHolder.getAvatarView().setColorFilter(ContextCompat.getColor(viewHolder.avatarView.getContext(), R.color.secondaryTextColor), PorterDuff.Mode.SRC_IN);
        }

        if (selected.contains(contact)) {
            viewHolder.itemView.setBackgroundResource(R.color.secondaryLightColor);
            viewHolder.itemView.setElevation(10);
        } else {
            viewHolder.itemView.setBackground(null);
            viewHolder.itemView.setElevation(0);
        }

        viewHolder.itemView.setOnClickListener((view) -> {
            if (selected.contains(contact)) {
                viewHolder.itemView.setBackground(null);
                viewHolder.itemView.setElevation(0);
                selected.remove(contact);
            } else {
                viewHolder.itemView.setBackgroundResource(R.color.secondaryLightColor);
                viewHolder.itemView.setElevation(10);
                selected.add(contact);
            }
            clickListener.onItemClick(contact);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public List<Contact> getSelected() {
        return selected;
    }

}