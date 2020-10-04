package org.ostrya.presencepublisher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.initialization.InitializationHandler;
import org.ostrya.presencepublisher.schedule.Scheduler;
import org.ostrya.presencepublisher.ui.MainPagerAdapter;

import java.util.Collections;

import static org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference.OFFLINE_CONTENT;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference.WIFI_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.connection.ClientCertificatePreference.CLIENT_CERTIFICATE;
import static org.ostrya.presencepublisher.ui.preference.connection.HostPreference.HOST;
import static org.ostrya.presencepublisher.ui.preference.connection.PasswordPreference.PASSWORD;
import static org.ostrya.presencepublisher.ui.preference.connection.PortPreference.PORT;
import static org.ostrya.presencepublisher.ui.preference.connection.UseTlsPreference.USE_TLS;
import static org.ostrya.presencepublisher.ui.preference.connection.UsernamePreference.USERNAME;
import static org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference.AUTOSTART;
import static org.ostrya.presencepublisher.ui.preference.schedule.BatteryTopicPreference.BATTERY_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.PresenceTopicPreference.PRESENCE_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.schedule.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener = this::onSharedPreferenceChanged;
    private final InitializationHandler handler = InitializationHandler.getHandler(InitializationHandler.HANDLER_CHAIN);
    private boolean locationServiceNeeded;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        HyperLog.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mainPagerAdapter);

        locationServiceNeeded = ((Application) getApplication()).supportsBeacons()
                // for WiFi name resolution
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);

        handler.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
        handler.initialize(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            handler.handleResult(this, requestCode, grantResults[0]);
        } else {
            HyperLog.w(TAG, "Permission request cancelled for request code " + requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handler.handleResult(this, requestCode, resultCode);
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
            case SEND_BATTERY_MESSAGE:
            case SEND_OFFLINE_MESSAGE:
            case SEND_VIA_MOBILE_NETWORK:
            case SSID_LIST:
            case USERNAME:
            case USE_TLS:
                HyperLog.i(TAG, "Changed parameter " + key);
                new Scheduler(this).scheduleNow();
                break;
            case AUTOSTART:
            case LAST_SUCCESS:
            case NEXT_SCHEDULE:
                break;
            default:
                if (key.startsWith(WIFI_CONTENT_PREFIX)) {
                    HyperLog.i(TAG, "Changed parameter " + key);
                    new Scheduler(this).scheduleNow();
                } else {
                    HyperLog.v(TAG, "Ignoring unexpected value " + key);
                }
        }
    }
}
