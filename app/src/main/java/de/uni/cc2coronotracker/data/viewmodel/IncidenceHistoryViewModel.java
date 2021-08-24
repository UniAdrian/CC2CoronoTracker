package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.data.models.Contact;
import de.uni.cc2coronotracker.data.models.Exposure;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.helper.ContextMediator;
import de.uni.cc2coronotracker.helper.IncidenceRVItems;

@HiltViewModel
public class IncidenceHistoryViewModel extends ViewModel {
    private ExposureRepository exposureRepository;
    private ContactRepository contactRepository;
    private ContextMediator ctxMediator;

    private MutableLiveData<List<CalendarViewModel.ExposureDisplayInfo>> exposureInfo = new MutableLiveData<>();
    private HashMap<String, List<CalendarViewModel.ExposureDisplayInfo>> groupedHashMap = new HashMap<>();
    private LiveData<List<IncidenceRVItems.ListItem>> consolidatedList;
    public MutableLiveData<List<CalendarViewModel.ExposureDisplayInfo>> getExposureInfo () {
        return exposureInfo;
    }
    public LiveData<List<IncidenceRVItems.ListItem>> getConsolidatedList () {
        return consolidatedList;
    }

    @Inject
    public IncidenceHistoryViewModel(ContextMediator ctxMediator,
                                     @NonNull ExposureRepository exposureRepository,
                                     @NonNull ContactRepository contactRepository) {
        this.ctxMediator = ctxMediator;
        this.exposureRepository = exposureRepository;
        this.contactRepository = contactRepository;
        consolidatedList = Transformations.map(exposureInfo, input -> {
            groupedHashMap = groupDataIntoHashMap(input);
            List<IncidenceRVItems.ListItem> rvItemlist = new ArrayList<>();
            for (String date : groupedHashMap.keySet()) {
                IncidenceRVItems.DateItem dateItem = new IncidenceRVItems.DateItem();
                dateItem.setDate(date);
                rvItemlist.add(dateItem);

                for (CalendarViewModel.ExposureDisplayInfo info : groupedHashMap.get(date)) {
                    IncidenceRVItems.GeneralItem generalItem = new IncidenceRVItems.GeneralItem();
                    generalItem.setInfo(info);
                    rvItemlist.add(generalItem);
                }
            }
            return rvItemlist;
        });
    }

    private HashMap<String, List<CalendarViewModel.ExposureDisplayInfo>> groupDataIntoHashMap(List<CalendarViewModel.ExposureDisplayInfo> listOfInfo) {
        HashMap<String, List<CalendarViewModel.ExposureDisplayInfo>> groupedHashMap = new HashMap<>();
        for(CalendarViewModel.ExposureDisplayInfo info : listOfInfo) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String hashMapKey = dateFormat.format(info.exposureData.startDate);
            if(groupedHashMap.containsKey(hashMapKey)) {
                Objects.requireNonNull(groupedHashMap.get(hashMapKey)).add(info);
            } else {
                List<CalendarViewModel.ExposureDisplayInfo> list = new ArrayList<>();
                list.add(info);
                groupedHashMap.put(hashMapKey, list);
            }
        }
        return groupedHashMap;
    }

    public void fetchExposures () {
        exposureRepository.getExposuresAll(queryResult -> {
            if (queryResult instanceof Result.Success) {
                List<Exposure> exposures = ((Result.Success<List<Exposure>>) queryResult).data;
                List<CalendarViewModel.ExposureDisplayInfo> e = new ArrayList<>();
                if (exposures.size() != 0) {
                    for (Exposure exposure : exposures) {
                        Contact contact = contactRepository.getContactSync(exposure.contactId);
                        e.add(new CalendarViewModel.ExposureDisplayInfo(exposure, contact.displayName, contact.photoUri));
                    }
                    exposureInfo.postValue(e);
                }
            } else {
                Exception e = ((Result.Error<?>) queryResult).exception;
                Log.e("IncidenceHistory", "Failed to fetch exposures.", e);
                exposureInfo.postValue(new ArrayList<>());
            }
        });
    }
}