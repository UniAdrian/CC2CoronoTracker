package de.uni.cc2coronotracker.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Exposure;

public class ExposureAdapter extends RecyclerView.Adapter<ExposureAdapter.ViewHolder> {

    private final List<Exposure> exposures;
    private final Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.txtDate);
        }

        public TextView getTextView() {
            return textView;
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
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.exposure_rv_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(getExposureDesc(position));
    }

    private String getExposureDesc(int position) {
        Date date = exposures.get(position).date;
        return context.getResources().getString(R.string.exposure_description, DateFormat.getDateTimeInstance().format(date));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return exposures.size();
    }
}