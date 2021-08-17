package de.uni.cc2coronotracker.data.adapters;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.dao.ContactDao;
import de.uni.cc2coronotracker.data.models.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "ContactAdapter";

    public interface OnItemClickListener {
        void onItemClick(Contact item);
    }

    private final List<ContactDao.ContactWithExposures> allContacts;
    private List<ContactDao.ContactWithExposures> filteredContacts;
    private final OnItemClickListener clickListener;

    private Drawable defaultAvatar = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView displayNameView;
        private final TextView descriptionView;
        private final ImageView avatarView;
        private final ImageButton favoriteView;

        private Contact contact;

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

        public Contact getContact() {return contact;}
        public void setContact(Contact contact) {this.contact = contact;}
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet List<Contact> containing the data to populate views to be used
     * by RecyclerView.
     */
    public ContactAdapter(List<ContactDao.ContactWithExposures> dataSet, OnItemClickListener listener) {
        allContacts = dataSet;
        filteredContacts = new ArrayList<>(allContacts);
        clickListener = listener;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return filteredContacts.get(position).contact.id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_rv_item, viewGroup, false);

        if (defaultAvatar == null) {
            defaultAvatar = ResourcesCompat.getDrawable(viewGroup.getContext().getResources(), R.drawable.ic_no_avatar_128, viewGroup.getContext().getTheme());
        }

        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener((v) -> clickListener.onItemClick(viewHolder.getContact()));

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ContactDao.ContactWithExposures contactWithExposures = filteredContacts.get(position);
        Contact contact = contactWithExposures.contact;

        viewHolder.setContact(contact);

        long numberOfExposures = contactWithExposures.exposures.size();
        String description = viewHolder.getFavoriteView().getContext().getResources().getString(R.string.contact_description_some, numberOfExposures);
        if (numberOfExposures == 0) {
            description = viewHolder.getFavoriteView().getContext().getResources().getString(R.string.description_no_exposures);
        }

        viewHolder.getDisplayNameView().setText(contact.displayName);
        viewHolder.getDescriptionView().setText(description);

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
    }

    @Override
    public int getItemCount() {
        return filteredContacts.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredContacts = allContacts;
                } else {
                    List<ContactDao.ContactWithExposures> filteredList = new ArrayList<>();
                    for (ContactDao.ContactWithExposures row : allContacts) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.contact.displayName.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    filteredContacts = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredContacts;
                return filterResults;
            }

            @SuppressLint({"NotifyDataSetChanged", "unchecked"})
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredContacts = (ArrayList<ContactDao.ContactWithExposures>) filterResults.values;
                notifyDataSetChanged();
            }

        };
    }

}