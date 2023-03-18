package org.ostrya.presencepublisher.log;

import org.altbeacon.beacon.logging.Logger;

public class BeaconLoggerAdapter implements Logger {
    private static final String BEACON_TAG_PREFIX = "altbeacon/";

    public void v(String tag, String message, Object... args) {
        DatabaseLogger.v(BEACON_TAG_PREFIX + tag, formatString(message, args));
    }

    public void v(Throwable t, String tag, String message, Object... args) {
        DatabaseLogger.v(BEACON_TAG_PREFIX + tag, formatString(message, args), t);
    }

    public void d(String tag, String message, Object... args) {
        DatabaseLogger.d(BEACON_TAG_PREFIX + tag, formatString(message, args));
    }

    public void d(Throwable t, String tag, String message, Object... args) {
        DatabaseLogger.d(BEACON_TAG_PREFIX + tag, formatString(message, args), t);
    }

    public void i(String tag, String message, Object... args) {
        DatabaseLogger.i(BEACON_TAG_PREFIX + tag, formatString(message, args));
    }

    public void i(Throwable t, String tag, String message, Object... args) {
        DatabaseLogger.i(BEACON_TAG_PREFIX + tag, formatString(message, args), t);
    }

    public void w(String tag, String message, Object... args) {
        DatabaseLogger.w(BEACON_TAG_PREFIX + tag, formatString(message, args));
    }

    public void w(Throwable t, String tag, String message, Object... args) {
        DatabaseLogger.w(BEACON_TAG_PREFIX + tag, formatString(message, args), t);
    }

    public void e(String tag, String message, Object... args) {
        DatabaseLogger.e(BEACON_TAG_PREFIX + tag, formatString(message, args));
    }

    public void e(Throwable t, String tag, String message, Object... args) {
        DatabaseLogger.e(BEACON_TAG_PREFIX + tag, formatString(message, args), t);
    }

    private String formatString(String message, Object... args) {
        return args.length == 0 ? message : String.format(message, args);
    }
}
