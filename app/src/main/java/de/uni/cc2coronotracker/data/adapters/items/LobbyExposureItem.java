package de.uni.cc2coronotracker.data.adapters.items;

import android.view.View;

import androidx.annotation.NonNull;

import com.xwray.groupie.viewbinding.BindableItem;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.LobbyViewModel;
import de.uni.cc2coronotracker.databinding.LobbyRvItemBinding;

public class LobbyExposureItem extends BindableItem<LobbyRvItemBinding> {

    private LobbyViewModel.ExposureContactPair exposure;
    public LobbyExposureItem(LobbyViewModel.ExposureContactPair exposure) {
        this.exposure = exposure;
    }

    @NonNull
    @Override
    protected LobbyRvItemBinding initializeViewBinding(@NonNull View view) {
        return LobbyRvItemBinding.bind(view);
    }

    @Override public void bind(LobbyRvItemBinding binding, int position) {
        binding.setExposurePair(exposure);
    }

    @Override public int getLayout() {
        return R.layout.lobby_rv_item;
    }
}