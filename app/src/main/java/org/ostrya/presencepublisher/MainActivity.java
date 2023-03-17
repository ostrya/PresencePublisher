package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.ui.initialization.InitializationHandler.HANDLER_CHAIN;
import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;
import org.ostrya.presencepublisher.ui.MainPagerAdapter;
import org.ostrya.presencepublisher.ui.initialization.InitializationHandler;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener =
            this::onSharedPreferenceChanged;
    private InitializationHandler handler;
    private boolean locationPermissionNeeded;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DatabaseLogger.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mainPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, mainPagerAdapter).attach();

        locationPermissionNeeded =
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

    public boolean isLocationPermissionNeeded() {
        return locationPermissionNeeded;
    }

    public boolean isBluetoothBeaconConfigured() {
        return !sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()).isEmpty();
    }

    private void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (LOCATION_CONSENT.equals(key)) {
            handleConsentChange();
        }
    }

    private void handleConsentChange() {
        if (sharedPreferences.getBoolean(LOCATION_CONSENT, false)) {
            DatabaseLogger.i(TAG, "User consented to location access, initializing.");
            handler.initialize();
        } else {
            DatabaseLogger.i(TAG, "User revoked location access consent, stopping schedule.");
            new Scheduler(this).stopSchedule();
        }
    }
}
