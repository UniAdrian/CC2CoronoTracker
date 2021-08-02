package de.uni.cc2coronotracker.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.sql.Date;
import java.util.List;

@Dao
public abstract class StatisticsDao {

    @Query("SELECT DATE(date / 1000, 'unixepoch') AS day,\n" +
            "       (date - :leastDate) / 86400000 AS dayDiff,\n" +
            "       COUNT(*) AS numExposures\n" +
            " FROM   exposures\n" +
            " WHERE dayDiff >= 0\n" +
            " GROUP BY DATE(date / 1000, 'unixepoch')\n" +
            " ORDER BY day")
    public abstract List<NumExposuresByDay> getExposuresByDay(Date leastDate);

    public static class NumExposuresByDay {
        public String day;
        public long dayDiff;
        public long numExposures;
    }
}
