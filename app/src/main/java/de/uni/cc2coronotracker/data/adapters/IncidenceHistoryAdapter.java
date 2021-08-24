package de.uni.cc2coronotracker.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;
import de.uni.cc2coronotracker.helper.IncidenceRVItems;

public class IncidenceHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final List<IncidenceRVItems.ListItem> consolidatedList;
    final Context context;

    public IncidenceHistoryAdapter(List<IncidenceRVItems.ListItem> consolidatedList, Context ctx) {
        this.consolidatedList = consolidatedList;
        this.context = ctx;
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        final TextView dateView;
        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.dateTextView);
        }
    }
    public class GeneralViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;
        final TextView infoTextView;
        final ImageView avatarView;
        public GeneralViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.exposureListRVName);
            infoTextView = itemView.findViewById(R.id.exposureListRVInfo);
            avatarView = itemView.findViewById(R.id.exposureListRVAvatar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View view = null;
        switch (viewType) {
            case IncidenceRVItems.ListItem.TYPE_DATE:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.fragment_exposure_date, parent, false);
                viewHolder = new DateViewHolder(view);
                break;
            case IncidenceRVItems.ListItem.TYPE_GENERAL:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.fragment_exposure_list_rv_item, parent, false);
                viewHolder = new GeneralViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case IncidenceRVItems.ListItem.TYPE_DATE:
                DateViewHolder dateViewHolder = (DateViewHolder) holder;
                IncidenceRVItems.DateItem dateItem = (IncidenceRVItems.DateItem) consolidatedList.get(position);
                dateViewHolder.dateView.setText(dateItem.getDate());
                break;
            case IncidenceRVItems.ListItem.TYPE_GENERAL:
                GeneralViewHolder generalViewHolder = (GeneralViewHolder) holder;
                IncidenceRVItems.GeneralItem generalItem = (IncidenceRVItems.GeneralItem) consolidatedList.get(position);
                CalendarViewModel.ExposureDisplayInfo info = generalItem.getInfo();
                generalViewHolder.titleTextView.setText(computeTitle(info));
                generalViewHolder.infoTextView.setText(computeInfo(info));
                generalViewHolder.avatarView.setImageURI(info.contactPhotoUri);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return consolidatedList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        int size = consolidatedList.size();
        return size;
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
        return String.format(Locale.GERMANY, "%d:%d", c.get(Calendar.HOUR), c.get(Calendar.MINUTE));
    }
}
