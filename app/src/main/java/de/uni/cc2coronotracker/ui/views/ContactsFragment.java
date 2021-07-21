package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.adapters.ContactAdapter;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.viewmodel.ContactViewModel;
import de.uni.cc2coronotracker.databinding.FragmentContactsBinding;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class ContactsFragment extends Fragment implements SearchView.OnQueryTextListener {

    private ContactViewModel contactsViewModel;

    private FragmentContactsBinding binding;

    private ActivityResultLauncher<Void> getContactLauncher;

    private boolean fabMenuOpen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactsViewModel = new ViewModelProvider(this.getActivity()).get(ContactViewModel.class);
        getContactLauncher = registerForActivityResult(new ActivityResultContracts.PickContact(), uri -> {
            contactsViewModel.onContactPick(uri);
        });

        setHasOptionsMenu(true);
    }

    private void onContactClicked(Contact c) {
        gotoContactDetails(c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);

        binding.setContactVM(contactsViewModel);
        binding.setLifecycleOwner(this);

        binding.contactListRV.setAdapter(new ContactAdapter(new ArrayList<>(), this::onContactClicked));

        binding.fabAddImport.setOnClickListener(view -> {
            if(!fabMenuOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });

        contactsViewModel.getRequestContactPick().observe(getViewLifecycleOwner(), event -> {
            if (event.isHandled()) return;
            // Returns Void, but we need to set the handled flag. ;)
            event.getContentIfNotHandled();

            getContactLauncher.launch(null);
        });

        contactsViewModel.getAllContactsWithExposures().observe(getViewLifecycleOwner(), contacts -> {
            binding.contactListRV.setAdapter(new ContactAdapter(contacts, this::onContactClicked));
            binding.contactListRV.getAdapter().notifyDataSetChanged();
        });

        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);


    }

    @Override
    public boolean onQueryTextChange(String query) {
        Log.d("DBG", "Query String: " + query);
        ((ContactAdapter)binding.contactListRV.getAdapter()).getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private void gotoContactDetails(Contact c) {
        ContactsFragmentDirections.ActionContactsToContactDetailsFragment navAction = ContactsFragmentDirections.actionContactsToContactDetailsFragment();
        navAction.setContactId(c.id);
        Navigation.findNavController(getView()).navigate(navAction);
    }

    private void showFABMenu(){
        fabMenuOpen=true;
        binding.fabAddImport.animate().rotation(135.0F);
        binding.fabAddContact.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        binding.fabImportSingle.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        binding.fabImportContacts.animate().translationY(-getResources().getDimension(R.dimen.standard_155));

        binding.lblAddNew.animate().setStartDelay(75).alpha(1.f).translationY(-getResources().getDimension(R.dimen.standard_55)).withStartAction(() -> binding.lblAddNew.setVisibility(View.VISIBLE));
        binding.lblImportSingle.animate().setStartDelay(150).alpha(1.f).translationY(-getResources().getDimension(R.dimen.standard_105)).withStartAction(() -> binding.lblImportSingle.setVisibility(View.VISIBLE));
        binding.lblImportAll.animate().setStartDelay(225).alpha(1.f).translationY(-getResources().getDimension(R.dimen.standard_155)).withStartAction(() -> binding.lblImportAll.setVisibility(View.VISIBLE));
    }

    private void closeFABMenu(){
        fabMenuOpen=false;
        binding.fabAddImport.animate().rotation(0);
        binding.fabAddContact.animate().translationY(0);
        binding.fabImportSingle.animate().translationY(0);
        binding.fabImportContacts.animate().translationY(0);

        binding.lblAddNew.animate().setStartDelay(0).alpha(0.f).translationY(0).withEndAction(() -> binding.lblAddNew.setVisibility(View.GONE));
        binding.lblImportAll.animate().setStartDelay(0).alpha(0.f).translationY(0).withEndAction(() -> binding.lblImportAll.setVisibility(View.GONE));
        binding.lblImportSingle.animate().setStartDelay(0).alpha(0.f).translationY(0).withEndAction(() -> binding.lblImportSingle.setVisibility(View.GONE));
    }
}