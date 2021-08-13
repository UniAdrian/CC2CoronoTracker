package de.uni.cc2coronotracker.data.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LobbyViewModel extends ViewModel {

    @Inject
    public LobbyViewModel() {
    }


    public static class ProgressiveExposure {
        public Date start;
        public Date end;
        public UUID uuid;
    }
}