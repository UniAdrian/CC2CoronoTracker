package de.uni.cc2coronotracker.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.sql.Date;
import java.util.List;

@Dao
public abstract class StatisticsDao {

    /**
     * Accumulates the number of exposures by day for all exposures after a given date.
     * @param leastDate The minimum date to be included in the result
     * @return A list of NumExposuresByDay
     */
    @Query("SELECT \n" +
            "    Date(start_date / 1000, 'unixepoch') AS day,\n" +
            "    start_date AS timestamp,\n" +
            "    JulianDay(start_date / 1000,  'unixepoch', 'start of day') - JulianDay(:leastDate / 1000, 'unixepoch', 'start of day')  AS dayDiff,\n" +
            "    COUNT(*) AS numExposures\n" +
            "FROM exposures\n" +
            "WHERE dayDiff >= 0\n" +
            "GROUP BY JulianDay(start_date / 1000, 'unixepoch', 'start of day')\n" +
            "ORDER BY day")
    public abstract List<NumExposuresByDay> getExposuresByDay(Date leastDate);

    /**
     *
     * @param minPercentage minimal percentage a contact has to have to be included as its own entry instead of 'other'
     * @return The list of exposures by contact (fulfilling the requirements)
     */
    @Query("SELECT display_name AS label,\n" +
            "COUNT(exposures.id) AS nExposures,\n" +
            "(SELECT COUNT(id) FROM exposures) AS nTotalExposures\n" +
            "FROM contacts\n" +
            "JOIN exposures\n" +
            "ON exposures.contact_id = contacts.id\n" +
            "GROUP BY contacts.id, contact_id\n" +
            "HAVING nTotalExposures / nExposures >= :minPercentage\n" +
            "ORDER BY nExposures\n"
    )
    public abstract List<NumExposuresByContact> getExposuresByContact(float minPercentage);

    @Query("SELECT COUNT(*) AS total,\n" +
            "   (SELECT COUNT(*) FROM exposures WHERE NOT location IS NULL) AS totalWithLocation\n" +
            "FROM exposures")
    public abstract GeneralExposureInfo getGeneralExposureInfo();


    public static class GeneralExposureInfo {
        public long total;
        public long totalWithLocation;
    }

    public static class NumExposuresByContact {
        public String label;
        public long nExposures;
        public long nTotalExposures;
    }

    public static class NumExposuresByDay {
        public String day;
        public long timestamp;
        public long dayDiff;
        public long numExposures;
    }
}
