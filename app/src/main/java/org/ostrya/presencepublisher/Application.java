package org.ostrya.presencepublisher;

import android.util.Log;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.log.CustomLogFormat;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HyperLog.initialize(this, new CustomLogFormat(this));
        if (BuildConfig.DEBUG) {
            HyperLog.setLogLevel(Log.VERBOSE);
        } else {
            HyperLog.setLogLevel(Log.INFO);
        }
    }
}
