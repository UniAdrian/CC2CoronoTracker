package de.uni.cc2coronotracker.data.adapters.items;

import android.view.View;

import androidx.annotation.NonNull;

import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.ExpandableItem;
import com.xwray.groupie.viewbinding.BindableItem;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.databinding.LobbyRvHeaderBinding;

public class LobbyHeaderItem  extends BindableItem<LobbyRvHeaderBinding>  implements ExpandableItem {

    private Contact contact;
    private ExpandableGroup expandableGroup;

    public LobbyHeaderItem(Contact contact) {
        this.contact = contact;
    }

    @NonNull
    @Override
    protected LobbyRvHeaderBinding initializeViewBinding(@NonNull View view) {
        return LobbyRvHeaderBinding.bind(view);
    }

    @Override public void bind(LobbyRvHeaderBinding binding, int position) {
        binding.setContact(contact);
        binding.lobbyRvHeader.setOnClickListener(view -> {
            expandableGroup.onToggleExpanded();
        });
    }

    @Override public int getLayout() {
        return R.layout.lobby_rv_header;
    }

    @Override
    public void setExpandableGroup(@NonNull ExpandableGroup onToggleListener) {
        this.expandableGroup = onToggleListener;
    }
}
