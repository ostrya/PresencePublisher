package org.ostrya.presencepublisher.mqtt.context.device;

import static android.location.LocationManager.PASSIVE_PROVIDER;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;

import java.util.Locale;

public class LocationProvider {
    private static final String TAG = "LocationProvider";
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
        Location location = locationManager.getLastKnownLocation(PASSIVE_PROVIDER);
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
