package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import androidx.preference.MultiSelectListPreference;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.message.wifi.SsidUtil;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;

import java.util.List;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.LIST;

public class SsidListPreference extends MultiSelectListPreference {
    public static final String SSID_LIST = "ssids";

    public SsidListPreference(Context context) {
        super(context);
        List<String> knownSsids = SsidUtil.getKnownSsids(context);
        String[] entryValues = knownSsids.toArray(new String[0]);
        setKey(SSID_LIST);
        setTitle(R.string.ssid_list_title);
        setSummaryProvider(new ExplanationSummaryProvider(R.string.ssid_list_summary, LIST));
        setEntryValues(entryValues);
        setEntries(entryValues);
        setIconSpaceReserved(false);
        setDialogTitle(R.string.ssid_list_title);
    }
}
