package de.uni.cc2coronotracker.data.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.dao.StatisticsDao;
import de.uni.cc2coronotracker.data.repositories.StatisticsRepository;
import de.uni.cc2coronotracker.data.repositories.async.RepositoryCallback;
import de.uni.cc2coronotracker.data.repositories.async.Result;
import de.uni.cc2coronotracker.data.repositories.providers.ResourceProvider;

/**
 * Provides business logic for the {@link de.uni.cc2coronotracker.ui.views.StatisticsFragment}.
 * Mainly fetches and consumes data provided by the {@link StatisticsRepository} and publishes
 * it for the statistics fragment.
 */
@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private static final String TAG = "StatisticsVM";

    /**
     * Simple enum denoting the range of exposures to fetch
     */
    public enum EXPOSURE_RANGE {
        WEEK,
        MONTH,
        ALWAYS
    }

    private final StatisticsRepository statisticsRepository;
    private final ResourceProvider resourceProvider;

    private final MutableLiveData<EXPOSURE_RANGE> exposureRange = new MutableLiveData<>();
    private final MutableLiveData<ExposureRangeDataSet> exposuresByRange = new MutableLiveData<>();
    private final MutableLiveData<PieData> exposuresByContact = new MutableLiveData<>();
    private final MutableLiveData<StatisticsDao.GeneralExposureInfo> generalExposureInfo = new MutableLiveData<>();

    @Inject
    public StatisticsViewModel(StatisticsRepository statisticsRepository, ResourceProvider resourceProvider) {
        this.statisticsRepository = statisticsRepository;
        this.resourceProvider = resourceProvider;

        // initialize the data on creation.
        updateExposuresByDay(EXPOSURE_RANGE.MONTH);
        updateExposuresByContact();
        updateGeneralExposureInfo();
    }

    /**
     * Fetches and publishes general statistics as provided by {@link StatisticsRepository#getGeneralExposureInfo}</ref>
     */
    public void updateGeneralExposureInfo() {
        statisticsRepository.getGeneralExposureInfo(result -> {
            if (result instanceof Result.Success) {
                StatisticsDao.GeneralExposureInfo data = ((Result.Success<StatisticsDao.GeneralExposureInfo>) result).data;
                generalExposureInfo.postValue(data);
            } else {
                Log.e(TAG, "Failed to fetch exposures by contact", ((Result.Error<?>)result).exception);
                exposuresByContact.postValue(null);
            }
        });
    }

    /**
     * Fetches and publishes accumulated exposures by contact as provided by {@link StatisticsRepository#getExposuresByContact(float, RepositoryCallback)}</ref>
     * Automatically groups users under a certain threshold as 'Others'
     * Currently uses a default threshold of 5%.
     * @see #processExposuresByContact(List)
     */
    public void updateExposuresByContact() {
        statisticsRepository.getExposuresByContact(0.05f, result -> {
            if (result instanceof Result.Success) {
                List<StatisticsDao.NumExposuresByContact> data = ((Result.Success<List<StatisticsDao.NumExposuresByContact>>) result).data;
                exposuresByContact.postValue(processExposuresByContact(data));
            } else {
                Log.e(TAG, "Failed to fetch exposures by contact", ((Result.Error<?>)result).exception);
                exposuresByContact.postValue(null);
            }
        });
    }

    /**
     * Processes the raw repository data fetched by {@link #updateExposuresByContact()} and transforms
     * it into data usable by MP Android Chart.
     * @param data The raw data from the repository
     * @return The PieData for the chart.
     */
    private PieData processExposuresByContact(List<StatisticsDao.NumExposuresByContact> data) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = resourceProvider.getString(R.string.statistics_exposures_by_contact);
        String otherStr = resourceProvider.getString(R.string.statistics_other);

        // We fetch those anew every time so we do not have to react actively to night-mode changes.
        // This call happens infrequently enough to not be a performance hit.
        int[] colors = resourceProvider.getColors(R.array.statistics_colors);


        //input data and fit data into pie chart entry
        long n = 0;
        for(StatisticsDao.NumExposuresByContact entry : data){
            n+=entry.nExposures;
            pieEntries.add(new PieEntry(entry.nExposures, entry.label));
        }

        if (data.get(0).nTotalExposures - n > 0) {
            pieEntries.add(new PieEntry(data.get(0).nTotalExposures - n, otherStr));
        }

        // Create the data set and return it.
        PieDataSet dataSet = new PieDataSet(pieEntries, label);
        dataSet.setColors(colors);

        return new PieData(dataSet);
    }

    /**
     * Fetches and publishes accumulated exposures by a certain date range as given by {@code range} as provided by {@link StatisticsRepository#getExposuresByDay(java.sql.Date, RepositoryCallback)}
     * Automatically groups contacts under a certain threshold as 'Others'
     * Currently uses a default threshold of 5%.
     * @see #processExposuresByContact(List)
     * @param range The date range to be used
     */
    public void updateExposuresByDay(EXPOSURE_RANGE range) {
        // Don't do unnecessary work.
        if (range == exposureRange.getValue()) return;
        exposureRange.postValue(range);

        java.sql.Date leastDate;
        switch (range) {
            case ALWAYS:
                leastDate = always();
                break;
            case WEEK:
                leastDate = lastWeek();
                break;
            case MONTH:
                leastDate = lastMonth();
                break;

            default:
                leastDate = new java.sql.Date(0);
        }

        statisticsRepository.getExposuresByDay(leastDate, result -> {
            if (result instanceof Result.Success) {
                exposuresByRange.postValue(processNumExposuresByDay(((Result.Success<List<StatisticsDao.NumExposuresByDay>>) result).data, leastDate, range));
            } else {
                Log.e(TAG, "Failed querying exposuresByDay.", ((Result.Error<?>)result).exception);
            }
        });
    }

    /**
     * Processes the raw repository data fetched by {@link #updateExposuresByDay(EXPOSURE_RANGE)} and transforms
     * it into data usable by MP Android Chart.
     * @param data The raw data from the repository
     * @param leastDate The least possible date in the range
     * @param range The date range to be used
     * @return The ExposureRangeDataSet for the chart.
     * @apiNote The spacing of the graph on the x-axis will be only representable for ranges other than ALWAYS in order to save precious memory.
     */
    private ExposureRangeDataSet processNumExposuresByDay(List<StatisticsDao.NumExposuresByDay> data, Date leastDate, EXPOSURE_RANGE range) {
        ExposureRangeDataSet result = new ExposureRangeDataSet();
        if (data == null) {
            return result;
        }

        // Create the needed lists. Only allocate space for alwaysLabels if needed.
        List<Entry> barEntries = new ArrayList<>(data.size());
        List<String> alwaysLabels = null;
        if (range == EXPOSURE_RANGE.ALWAYS) {
            alwaysLabels = new ArrayList<>(data.size());
        }

        // Create the barEntries and pre-emptively fill the "alwaysLabels" list if needed.
        for (int i=0; i<data.size(); ++i) {
            Entry barEntry = new Entry((range != EXPOSURE_RANGE.ALWAYS) ? data.get(i).dayDiff : i, data.get(i).numExposures);
            barEntries.add(barEntry);

            if (range == EXPOSURE_RANGE.ALWAYS) {
                alwaysLabels.add(data.get(i).day);
            }
        }

        // Create or use the generated labels for the chart.
        // We differentiate between ALWAYS and the other mdoes, since always becomes potentially huge,
        // while the other are limited to 7 and 31 entries.
        List<String> labels = (range != EXPOSURE_RANGE.ALWAYS) ? prepLabelString(leastDate) : alwaysLabels;

        // Put it together
        String strExposures = resourceProvider.getString(R.string.statistics_label_exposures);
        result.data = new LineData(new LineDataSet(barEntries, strExposures));
        result.labels = labels;

        return result;
    }

    /**
     * Creates a list of date labels for {@link #processNumExposuresByDay(List, Date, EXPOSURE_RANGE)}
     * @param leastDate The least possible date in the range
     * @return A list of labels
     */
    private List<String> prepLabelString(Date leastDate) {
        Date endDate = new Date();
        long numDays = TimeUnit.DAYS.convert(endDate.getTime() - leastDate.getTime(), TimeUnit.MILLISECONDS);

        SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd", Locale.getDefault());

        List<String> labels = new ArrayList<>((int) numDays);
        for (int i = 0; i < numDays; ++i) {
            Date current = DateUtils.addDays(leastDate, i);
            labels.add(df.format(current));
        }

        return labels;
    }


    /**
     * @return The least possible date.
     */
    private java.sql.Date always() {
        return new java.sql.Date(0);
    }

    /**
     * @return The date exactly one week ago.
     */
    private java.sql.Date lastWeek() {
        Date result = DateUtils.addWeeks(new Date(),-1);
        return new java.sql.Date(result.getTime());
    }

    /**
     * @return The date exactly one month ago.
     */
    private java.sql.Date lastMonth() {
        Date result = DateUtils.addMonths(new Date(),-1);
        return new java.sql.Date(result.getTime());
    }


    public LiveData<EXPOSURE_RANGE> getExposureRange() {
        return exposureRange;
    }
    public LiveData<ExposureRangeDataSet> getExposureByRangeEntries() {
        return exposuresByRange;
    }

    public LiveData<PieData> getExposuresByContact() {
        return exposuresByContact;
    }

    public LiveData<StatisticsDao.GeneralExposureInfo> getGeneralExposureInfo() {
        return generalExposureInfo;
    }

    /**
     * A tuple of LineData and labels for publishing to the fragment(s).
     */
    public static class ExposureRangeDataSet {
        public List<String> labels;
        public LineData data;
    }
}