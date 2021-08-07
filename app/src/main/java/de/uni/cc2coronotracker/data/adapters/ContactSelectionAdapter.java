package de.uni.cc2coronotracker.data.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    private List<Contact> contacts;
    private List<Contact> selected;
    private OnItemClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_select_rv_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Contact contact = contacts.get(position);

        viewHolder.getDisplayNameView().setText(contact.displayName);
        if (contact.photoUri != null) {
            try {
                viewHolder.getAvatarView().setImageURI(contact.photoUri);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set image uri: ", e);
                viewHolder.getAvatarView().setImageResource(R.drawable.ic_no_avatar_128);
            }
        } else {
            viewHolder.getAvatarView().setImageResource(R.drawable.ic_no_avatar_128);
        }

        if (selected.contains(contact)) {
            viewHolder.itemView.setBackgroundResource(R.color.design_default_color_secondary);
            viewHolder.itemView.setElevation(10);
        } else {
            viewHolder.itemView.setBackgroundResource(R.color.primaryLightColor);
            viewHolder.itemView.setElevation(0);
        }

        viewHolder.itemView.setOnClickListener((view) -> {
            if (selected.contains(contact)) {
                viewHolder.itemView.setBackgroundResource(R.color.primaryLightColor);
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