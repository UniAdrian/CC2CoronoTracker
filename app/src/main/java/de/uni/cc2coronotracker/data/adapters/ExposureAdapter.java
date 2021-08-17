package de.uni.cc2coronotracker.data.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Exposure;

public class ExposureAdapter extends RecyclerView.Adapter<ExposureAdapter.ViewHolder> {

    private final List<Exposure> exposures;
    private final Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateDescTxt;
        private final TextView durationDescTxt;
        private final ImageView locEnabledImg;

        public ViewHolder(View view) {
            super(view);
            dateDescTxt = view.findViewById(R.id.txtDate);
            durationDescTxt = view.findViewById(R.id.txtDuration);
            locEnabledImg = view.findViewById(R.id.imgLocationEnabled);
        }

        public TextView getDateDescTxt() {
            return dateDescTxt;
        }
        public TextView getDurationDescTxt() {
            return durationDescTxt;
        }
        public ImageView getLocationIcon() {
            return locEnabledImg;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet List<Exposure></> containing the data to populate views to be used
     * by RecyclerView.
     */
    public ExposureAdapter(List<Exposure> dataSet, Context ctx) {
        exposures = dataSet;
        context = ctx;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.exposure_rv_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Exposure exp = exposures.get(position);

        int iconResource = R.drawable.ic_baseline_location_on_24;
        if (exp.location == null) {
            iconResource = R.drawable.ic_baseline_location_off_24;
        }

        viewHolder.getDateDescTxt().setText(getExposureDesc(exp));
        viewHolder.getDurationDescTxt().setText(getDurationDesc(exp));
        viewHolder.getLocationIcon().setImageResource(iconResource);
    }

    private String getDurationDesc(Exposure exp) {
        if (exp.endDate == null) {
            return context.getResources().getString(R.string.exposure_duration_not_finished_description);
        }

        Resources res = context.getResources();

        long durInMillis = exp.endDate.getTime() - exp.startDate.getTime();
        String format = res.getString(R.string.exposure_duration_format_string);
        String dur = DurationFormatUtils.formatDuration(durInMillis, format);
        return res.getString(R.string.exposure_duration_description, dur);
    }

    private String getExposureDesc(Exposure exp) {
        Date date = exp.startDate;
        return context.getResources().getString(R.string.exposure_description, DateFormat.getDateTimeInstance().format(date));
    }

    // Return the size of the dataset
    @Override
    public int getItemCount() {
        return exposures.size();
    }
}