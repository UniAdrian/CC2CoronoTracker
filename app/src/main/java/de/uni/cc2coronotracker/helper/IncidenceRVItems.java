package de.uni.cc2coronotracker.helper;

import de.uni.cc2coronotracker.data.viewmodel.CalendarViewModel;

public class IncidenceRVItems {
    public static class DateItem extends ListItem {
        private String date;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @Override
        public int getType() {
            return TYPE_DATE;
        }
    }

    public static class GeneralItem extends ListItem {
        private CalendarViewModel.ExposureDisplayInfo exposureDisplayInfo;

        public CalendarViewModel.ExposureDisplayInfo getInfo () {
            return this.exposureDisplayInfo;
        }
        public void setInfo (CalendarViewModel.ExposureDisplayInfo info) {
            this.exposureDisplayInfo = info;
        }
        @Override
        public int getType() {
            return TYPE_GENERAL;
        }
    }

    public abstract static class ListItem {
        final public static int TYPE_GENERAL = 0;
        final public static int TYPE_DATE = 1;

        public abstract int getType();
    }
}


