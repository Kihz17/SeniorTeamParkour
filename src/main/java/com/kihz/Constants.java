package com.kihz;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Constants {
    public static final String PLUGIN_NAME = "Parkour";
    public static final String NMS_VERSION = "v1_18_R2";
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("America/New_York");

    public static final int MS_PER_SECOND = (int) TimeUnit.SECONDS.toMillis(1);
    public static final int MS_PER_MINUTE = (int) TimeUnit.MINUTES.toMillis(1);
    public static final int MS_PER_HOUR = (int) TimeUnit.HOURS.toMillis(1);
    public static final int TPS = 20; // Ticks per second.
    public static final int TPM = 60 * TPS; // Ticks per minute.
    public static final int TPH = 60 * TPM; // Ticks per hour.
    public static final int TICK_MS = MS_PER_SECOND / TPS;
}
