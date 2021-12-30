package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.ui.initialization.InitializationHandler.HANDLER_CHAIN;
import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference.OFFLINE_CONTENT;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.WIFI_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.connection.ClientCertificatePreference.CLIENT_CERTIFICATE;
import static org.ostrya.presencepublisher.ui.preference.connection.HostPreference.HOST;
import static org.ostrya.presencepublisher.ui.preference.connection.PasswordPreference.PASSWORD;
import static org.ostrya.presencepublisher.ui.preference.connection.PortPreference.PORT;
import static org.ostrya.presencepublisher.ui.preference.connection.QoSPreference.QOS_VALUE;
import static org.ostrya.presencepublisher.ui.preference.connection.RetainFlagPreference.RETAIN_FLAG;
import static org.ostrya.presencepublisher.ui.preference.connection.UseTlsPreference.USE_TLS;
import static org.ostrya.presencepublisher.ui.preference.connection.UsernamePreference.USERNAME;
import static org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference.AUTOSTART;
import static org.ostrya.presencepublisher.ui.preference.schedule.BatteryTopicPreference.BATTERY_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.PresenceTopicPreference.PRESENCE_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.schedule.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.schedule.Scheduler;
import org.ostrya.presencepublisher.ui.MainPagerAdapter;
import org.ostrya.presencepublisher.ui.initialization.InitializationHandler;

import java.util.Collections;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener =
            this::onSharedPreferenceChanged;
    private InitializationHandler handler;
    private boolean locationServiceNeeded;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        HyperLog.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mainPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, mainPagerAdapter).attach();

        locationServiceNeeded =
                ((PresencePublisher) getApplication()).supportsBeacons()
                        // for Wi-Fi name resolution
                        || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
        handler = InitializationHandler.getHandler(this, HANDLER_CHAIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
        handler.initialize();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    public boolean isLocationServiceNeeded() {
        return locationServiceNeeded;
    }

    public boolean isBluetoothBeaconConfigured() {
        return !sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()).isEmpty();
    }

    private void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        switch (key) {
            case BEACON_LIST:
            case BATTERY_TOPIC:
            case CLIENT_CERTIFICATE:
            case HOST:
            case MESSAGE_SCHEDULE:
            case OFFLINE_CONTENT:
            case PASSWORD:
            case PORT:
            case PRESENCE_TOPIC:
            case QOS_VALUE:
            case RETAIN_FLAG:
            case SEND_BATTERY_MESSAGE:
            case SEND_OFFLINE_MESSAGE:
            case SEND_VIA_MOBILE_NETWORK:
            case SSID_LIST:
            case USERNAME:
            case USE_TLS:
                onChangedConnectionProperty(key);
                break;
            case AUTOSTART:
            case LAST_SUCCESS:
            case NEXT_SCHEDULE:
                break;
            case LOCATION_CONSENT:
                handleConsentChange();
                break;
            default:
                if (key.startsWith(WIFI_CONTENT_PREFIX) || key.startsWith(BEACON_CONTENT_PREFIX)) {
                    onChangedConnectionProperty(key);
                } else {
                    HyperLog.d(TAG, "Ignoring unexpected value " + key);
                }
        }
    }

    private void onChangedConnectionProperty(String key) {
        HyperLog.i(TAG, "Changed parameter " + key);
        new Scheduler(this).scheduleNow();
    }

    private void handleConsentChange() {
        if (sharedPreferences.getBoolean(LOCATION_CONSENT, false)) {
            HyperLog.i(TAG, "User consented to location access, initializing.");
            handler.initialize();
        } else {
            HyperLog.i(TAG, "User revoked location access consent, stopping schedule.");
            new Scheduler(this).stopSchedule();
        }
    }
}
