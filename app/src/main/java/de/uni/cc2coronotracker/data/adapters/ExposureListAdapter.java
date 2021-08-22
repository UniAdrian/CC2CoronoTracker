package de.uni.cc2coronotracker.data.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;

public class ExposureListAdapter extends RecyclerView.Adapter<ExposureListAdapter.ExposureViewHolder> {
    final List<CalendarViewModel.ExposureDisplayInfo> data;
    final Context context;


    public interface OnItemClickListener {
        void onItemClick(CalendarViewModel.ExposureDisplayInfo exposureDisplayInfo);
    }
    private OnItemClickListener listener;
    public ExposureListAdapter(List<CalendarViewModel.ExposureDisplayInfo> data, Context ctx, OnItemClickListener listener) {
        this.data = data;
        this.context = ctx;
        this.listener = listener;
    }

    public static class ExposureViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;
        final TextView infoTextView;
        final ImageView avatarView;
        public ExposureViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.exposureListRVName);
            infoTextView = itemView.findViewById(R.id.exposureListRVInfo);
            avatarView = itemView.findViewById(R.id.exposureListRVAvatar);
        }
        private CalendarViewModel.ExposureDisplayInfo currentEntry;
        public CalendarViewModel.ExposureDisplayInfo getCurrentEntry() {
            return currentEntry;
        }
        public void setCurrentEntry(CalendarViewModel.ExposureDisplayInfo entry) {
            currentEntry = entry;
        }
    }

    @NonNull
    @Override
    public ExposureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_exposure_list_rv_item, parent, false);
        ExposureViewHolder viewHolder = new ExposureViewHolder(view);
        viewHolder.itemView.setOnClickListener((v) -> {
            listener.onItemClick(viewHolder.getCurrentEntry());
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExposureViewHolder holder, int position) {
        CalendarViewModel.ExposureDisplayInfo currentEntry = data.get(position);
        holder.setCurrentEntry(currentEntry);
        holder.titleTextView.setText(computeTitle(currentEntry));
        holder.infoTextView.setText(computeInfo(currentEntry));
        holder.avatarView.setImageURI(currentEntry.contactPhotoUri);
        holder.itemView.setOnClickListener(view -> {
            listener.onItemClick(currentEntry);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    private String computeTitle(CalendarViewModel.ExposureDisplayInfo exposureInfo) {
        return String.valueOf(exposureInfo.contactName);
    }
    private String computeInfo(CalendarViewModel.ExposureDisplayInfo exposureInfo) {
        return String.format("Time: %s, Location: %s",
                computeTimeForm(exposureInfo.exposureData.startDate),
                exposureInfo.exposureData.location == null ? "Not available" : exposureInfo.exposureData.location.toString());
    }
    private String computeTimeForm(Date date) {
        long millis = date.getTime();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return String.format(Locale.GERMANY, "%02d:%02d", c.get(Calendar.HOUR), c.get(Calendar.MINUTE));
    }
}