package org.ostrya.presencepublisher.preference.about;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.BooleanPreferenceBase;

public class LocationConsentPreference extends BooleanPreferenceBase {
    public static final String LOCATION_CONSENT = "locationConsent";

    public LocationConsentPreference(Context context) {
        super(
                context,
                LOCATION_CONSENT,
                R.string.location_consent_title,
                R.string.location_consent_summary);
    }
}
