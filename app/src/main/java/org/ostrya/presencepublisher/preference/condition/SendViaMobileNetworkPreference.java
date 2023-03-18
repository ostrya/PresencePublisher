package org.ostrya.presencepublisher.preference.condition;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.BooleanPreferenceBase;

public class SendViaMobileNetworkPreference extends BooleanPreferenceBase {
    public static final String SEND_VIA_MOBILE_NETWORK = "sendViaMobileNetwork";

    public SendViaMobileNetworkPreference(Context context) {
        super(
                context,
                SEND_VIA_MOBILE_NETWORK,
                R.string.send_via_mobile_network_title,
                R.string.send_via_mobile_network_summary);
    }
}
