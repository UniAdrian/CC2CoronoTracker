package de.uni.cc2coronotracker.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Contact item);
    }

    private List<Contact> contacts;
    private OnItemClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView displayNameView;
        private final TextView descriptionView;
        private final ImageView avatarView;
        private final ImageButton favoriteView;

        public ViewHolder(View view) {
            super(view);
            displayNameView = view.findViewById(R.id.crvDisplayName);
            descriptionView = view.findViewById(R.id.crvDescription);
            avatarView = view.findViewById(R.id.crvAvatar);
            favoriteView = view.findViewById(R.id.crvFavButton);
        }

        public TextView getDisplayNameView() {
            return displayNameView;
        }
        public TextView getDescriptionView() {
            return descriptionView;
        }
        public ImageView getAvatarView() {
            return avatarView;
        }
        public ImageButton getFavoriteView() {
            return favoriteView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet List<Contact> containing the data to populate views to be used
     * by RecyclerView.
     */
    public ContactAdapter(List<Contact> dataSet, OnItemClickListener listener) {
        contacts = dataSet;
        clickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_rv_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Contact contact = contacts.get(position);

        // TODO: Set valid description and fav state
        String description = viewHolder.getFavoriteView().getContext().getResources().getString(R.string.contact_description_some, 5);

        viewHolder.getDisplayNameView().setText(contact.displayName);
        viewHolder.getDescriptionView().setText(description);

        if (contact.photoUri != null) {
            viewHolder.getAvatarView().setImageURI(contact.photoUri);
        } else {
            viewHolder.getAvatarView().setImageResource(R.drawable.ic_no_avatar_128);
        }

        viewHolder.itemView.setOnClickListener((view) -> clickListener.onItemClick(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

}