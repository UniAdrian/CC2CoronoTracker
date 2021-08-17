package de.uni.cc2coronotracker.data.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.uni.cc2coronotracker.data.viewmodel.LobbyViewModel;
import de.uni.cc2coronotracker.databinding.LobbyRvItemBinding;

public class LobbyExposureAdapter extends RecyclerView.Adapter<LobbyExposureAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(LobbyViewModel.ProgressiveExposure item);
    }

    private final List<LobbyViewModel.ProgressiveExposure> exposures;
    private final OnItemClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements LifecycleOwner {
        private final LobbyRvItemBinding binding;
        private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

        public ViewHolder(LobbyRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            lifecycleRegistry.setCurrentState(Lifecycle.State.INITIALIZED);
        }

        public void bind(LobbyViewModel.ProgressiveExposure item) {
            binding.setExposure(item);
            binding.executePendingBindings();
        }

        public void markAttach() {
            lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        }

        public void markDetach() {
            lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        }

        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet List<CertEntity></> containing the data to populate views to be used
     * by RecyclerView.
     */
    public LobbyExposureAdapter(@NonNull List<LobbyViewModel.ProgressiveExposure> dataSet, @NonNull OnItemClickListener onItemClickListener) {
        exposures = dataSet;
        clickListener = onItemClickListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        LobbyRvItemBinding itemBinding = LobbyRvItemBinding.inflate(layoutInflater, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(itemBinding);
        itemBinding.setLifecycleOwner(viewHolder);
        viewHolder.itemView.setOnClickListener((v) -> clickListener.onItemClick(viewHolder.binding.getExposure()));

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        LobbyViewModel.ProgressiveExposure item = exposures.get(position);
        viewHolder.bind(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return exposures.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.markAttach();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.markDetach();
    }
}