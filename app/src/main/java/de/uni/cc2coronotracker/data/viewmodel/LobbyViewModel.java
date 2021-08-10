package de.uni.cc2coronotracker.data.viewmodel;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LobbyViewModel extends ViewModel {

    @Inject
    public LobbyViewModel() {
    }
}