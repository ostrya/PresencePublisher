package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.initialization.InitializationHandler.HANDLER_CHAIN;
import static org.ostrya.presencepublisher.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.ostrya.presencepublisher.initialization.InitializationHandler;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.preference.MainPagerAdapter;
import org.ostrya.presencepublisher.schedule.Scheduler;

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

        View parent = findViewById(R.id.parent);
        ViewCompat.setOnApplyWindowInsetsListener(
                parent,
                (v, insets) -> {
                    Insets bars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.displayCutout());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });

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
        handler.initialize();
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener);
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
        PresencePublisher application = (PresencePublisher) getApplication();
        if (sharedPreferences.getBoolean(LOCATION_CONSENT, false)) {
            DatabaseLogger.i(TAG, "User consented to location access, initializing.");
            application.setUpConditionCallbacks(sharedPreferences);
            handler.initialize();
        } else {
            DatabaseLogger.i(TAG, "User revoked location access consent, stopping schedule.");
            application.removeConditionCallbacks();
            new Scheduler(this).stopSchedule();
        }
    }
}
