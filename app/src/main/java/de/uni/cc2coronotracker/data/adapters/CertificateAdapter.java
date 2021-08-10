package de.uni.cc2coronotracker.data.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.uni.cc2coronotracker.data.models.CertEntity;
import de.uni.cc2coronotracker.databinding.CertRvItemBinding;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CertEntity item);
    }

    private final List<CertEntity> certs;
    private final OnItemClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements LifecycleOwner {
        private final CertRvItemBinding binding;
        private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

        public ViewHolder(CertRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            lifecycleRegistry.setCurrentState(Lifecycle.State.INITIALIZED);
        }

        public void bind(CertEntity item) {
            binding.setCert(item);
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
    public CertificateAdapter(@NonNull List<CertEntity> dataSet, @NonNull OnItemClickListener onItemClickListener) {
        certs = dataSet;
        clickListener = onItemClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        CertRvItemBinding itemBinding = CertRvItemBinding.inflate(layoutInflater, viewGroup, false);


        ViewHolder viewHolder = new ViewHolder(itemBinding);
        itemBinding.setLifecycleOwner(viewHolder);
        viewHolder.itemView.setOnClickListener((v) -> clickListener.onItemClick(viewHolder.binding.getCert()));

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        CertEntity item = certs.get(position);
        viewHolder.bind(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return certs.size();
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