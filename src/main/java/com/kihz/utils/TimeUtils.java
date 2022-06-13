package com.kihz.utils;

import com.kihz.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TimeUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy KK:mm a z");

    /**
     * Format seconds to H:M:S
     * @param seconds the amount of seconds to format
     * @return format
     */
    public static String formatSeconds(int seconds) {
        StringBuilder builder = new StringBuilder();
        int minutes = seconds / 60;
        int sec = seconds % 60;
        int hours = minutes / 60;

        if(hours > 0)
            builder.append(hours).append("h ");

        if(minutes > 0)
            builder.append(minutes).append("m ");

        if(sec > 0)
            builder.append(sec).append("s ");
        return builder.toString().trim();
    }

    /**
     * Format time from now
     * @param time The time to format
     * @param type The display type
     * @return
     */
    public static String formatTimeFromNow(long time, FormatType type) {
        return formatTime(time == FormatType.PERMANENT ? time : time - System.currentTimeMillis(), type);
    }

    /**
     * Return a formatted string to display time
     * @param time The time to format
     * @param setting The format type
     * @return formattedTime
     */
    public static String formatTime(long time, FormatType setting) {
        if (time == FormatType.PERMANENT)
            return setting.getFilter().apply("never");

        time /= Constants.MS_PER_SECOND;
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < TimeInterval.values().length; i++) {
            TimeInterval iv = TimeInterval.values()[TimeInterval.values().length - i - 1];
            if (time < iv.getInterval())
                continue; // Isn't good enough.

            int temp = (int) (time - (time % iv.getInterval()));
            int add = temp / iv.getInterval();
            display.append(" ").append(setting.getDisplay(iv, add));
            time -= temp;
        }

        return display.length() > 0 ? display.toString().substring(1) : setting.getDisplay(TimeInterval.SECOND, 0);
    }

    /**
     * Get the current date formatted nicely.
     * @return displayTime
     */
    public static String getDisplayTime() {
        DATE_FORMAT.setTimeZone(Constants.TIME_ZONE);
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    @AllArgsConstructor
    public enum FormatType {
        MINIMAL(String::toLowerCase, t -> t.getSuffix().substring(0, 1)),
        DEFAULT(String::toLowerCase, TimeInterval::getSuffix),
        FULL(Utils::capitalize, t -> " " + Utils.capitalize(t.name()));

        public static final int PERMANENT = -1;

        @Getter
        private final Function<String, String> filter;
        private final Function<TimeInterval, String> suffixFilter;

        /**
         * Returns the display string for this time.
         * @param interval The time interval
         * @param value The value (time)
         * @return display
         */
        public String getDisplay(TimeInterval interval, int value) {
            String suffix = suffixFilter.apply(interval); // Get the correct suffix
            if(value > 1 && !suffix.endsWith("s") && this != MINIMAL) // Add an s to the end if we are plural
                suffix += "s";
            return value + suffix;
        }
    }

    @AllArgsConstructor @Getter
    public enum TimeInterval {
        SECOND("s", TimeUnit.SECONDS, Calendar.SECOND),
        MINUTE("min", TimeUnit.MINUTES, Calendar.MINUTE),
        HOUR("hr", TimeUnit.HOURS, Calendar.HOUR_OF_DAY),
        DAY("day", TimeUnit.DAYS, Calendar.DAY_OF_MONTH),
        WEEK("week", 7, Calendar.WEEK_OF_MONTH),
        MONTH("month", 30, Calendar.MONTH),
        YEAR("yr", 365, Calendar.YEAR);

        private String suffix;
        private TimeUnit unit;
        private int interval;
        private int calendarId;

        TimeInterval(String s, TimeUnit unit, int calendar) {
            this(s, unit, (int) TimeUnit.SECONDS.convert(1, unit), calendar);
        }

        TimeInterval(String s, int days, int calendar) {
            this(s, null, (int) TimeUnit.SECONDS.convert(days, TimeUnit.DAYS), calendar);
        }

        public static TimeInterval getByCode(String code) {
            return Arrays.stream(values()).filter(ti -> ti.getSuffix().startsWith(code.toLowerCase())).findFirst().orElse(SECOND);
        }

        /**
         * Get the current time unit value for this interval.
         * @return unit
         */
        @SuppressWarnings("MagicConstant")
        public int getValue() {
            return Calendar.getInstance().get(getCalendarId());
        }
    }
}
