package org.ostrya.presencepublisher.beacon;

import com.hypertrack.hyperlog.HyperLog;
import org.altbeacon.beacon.logging.Logger;

public class HyperlogLogger implements Logger {
    public void v(String tag, String message, Object... args) {
        HyperLog.v(tag, formatString(message, args));
    }

    public void v(Throwable t, String tag, String message, Object... args) {
        HyperLog.v(tag, this.formatString(message, args), t);
    }

    public void d(String tag, String message, Object... args) {
        HyperLog.d(tag, formatString(message, args));
    }

    public void d(Throwable t, String tag, String message, Object... args) {
        HyperLog.d(tag, this.formatString(message, args), t);
    }

    public void i(String tag, String message, Object... args) {
        HyperLog.i(tag, formatString(message, args));
    }

    public void i(Throwable t, String tag, String message, Object... args) {
        HyperLog.i(tag, this.formatString(message, args), t);
    }

    public void w(String tag, String message, Object... args) {
        HyperLog.w(tag, formatString(message, args));
    }

    public void w(Throwable t, String tag, String message, Object... args) {
        HyperLog.w(tag, this.formatString(message, args), t);
    }

    public void e(String tag, String message, Object... args) {
        HyperLog.e(tag, formatString(message, args));
    }

    public void e(Throwable t, String tag, String message, Object... args) {
        HyperLog.e(tag, formatString(message, args), t);
    }

    private String formatString(String message, Object... args) {
        return args.length == 0 ? message : String.format(message, args);
    }
}
