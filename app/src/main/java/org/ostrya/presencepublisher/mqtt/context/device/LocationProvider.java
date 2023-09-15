package org.ostrya.presencepublisher.mqtt.context.device;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;

import java.util.Locale;

public class LocationProvider {
    private static final String TAG = "LocationProvider";
    private static final Criteria NO_CRITERIA = new Criteria();
    // inspired from https://datatracker.ietf.org/doc/html/rfc5870
    private static final String GEO_FORMAT = "geo:%f,%f;u=%f;timestamp=%d";
    private final LocationManager locationManager;

    public LocationProvider(Context applicationContext) {
        locationManager =
                (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public String getLastKnownLocation() {
        if (locationManager == null) {
            DatabaseLogger.w(TAG, "No location manager available, unable to get location");
            return MessageContext.UNKNOWN;
        }
        // according to Javadoc, this will give us the provider with best accuracy
        String provider = locationManager.getBestProvider(NO_CRITERIA, true);
        if (provider == null) {
            DatabaseLogger.w(TAG, "Unable to get location provider");
            return MessageContext.UNKNOWN;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location == null) {
            DatabaseLogger.w(TAG, "No last known location found");
            return MessageContext.UNKNOWN;
        }
        return String.format(
                Locale.ROOT,
                GEO_FORMAT,
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                location.getTime() / 1000);
    }
}
