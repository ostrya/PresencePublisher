package org.ostrya.presencepublisher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.mqtt.Publisher;
import org.ostrya.presencepublisher.ui.MainPagerAdapter;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static org.ostrya.presencepublisher.Application.*;
import static org.ostrya.presencepublisher.ui.preference.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.AutostartPreference.AUTOSTART;
import static org.ostrya.presencepublisher.ui.preference.BatteryTopicPreference.BATTERY_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.ClientCertificatePreference.CLIENT_CERTIFICATE;
import static org.ostrya.presencepublisher.ui.preference.HostPreference.HOST;
import static org.ostrya.presencepublisher.ui.preference.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.OfflineContentPreference.OFFLINE_CONTENT;
import static org.ostrya.presencepublisher.ui.preference.PasswordPreference.PASSWORD;
import static org.ostrya.presencepublisher.ui.preference.PortPreference.PORT;
import static org.ostrya.presencepublisher.ui.preference.PresenceTopicPreference.PRESENCE_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.UseTlsPreference.USE_TLS;
import static org.ostrya.presencepublisher.ui.preference.UsernamePreference.USERNAME;
import static org.ostrya.presencepublisher.ui.preference.WifiNetworkPreference.WIFI_CONTENT_PREFIX;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener = this::onSharedPreferenceChanged;
    private MainPagerAdapter mainPagerAdapter;
    private ViewPager viewPager;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        HyperLog.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mainPagerAdapter);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);

        checkLocationPermissionAndAccessAndBatteryOptimizationAndStartWorker();

        HyperLog.d(TAG, "Creating activity finished");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
        new Publisher(this).scheduleNow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Successfully granted location permission");
            checkLocationAccessAndBatteryOptimizationAndStartWorker();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE) {
            // For some reason, the activity returns RESULT_CANCELED even when the service is enabled, so we don't
            // know if it was actually enabled or not. For now, we don't check again and just start the service.
            HyperLog.d(TAG, "Returning from location service with result " + resultCode + ", assuming it is running ...");
            checkBatteryOptimizationAndStartWorker();
        } else if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
            HyperLog.d(TAG, "Returning from battery optimization with result " + resultCode + ", assuming it disabled ...");
            new Publisher(this).scheduleNow();
        }
    }

    private void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        switch (key) {
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
                new Publisher(this).scheduleNow();
                break;
            case AUTOSTART:
            case LAST_SUCCESS:
            case NEXT_SCHEDULE:
                break;
            default:
                if (key.startsWith(WIFI_CONTENT_PREFIX)) {
                    HyperLog.i(TAG, "Changed parameter " + key);
                    new Publisher(this).scheduleNow();
                } else {
                    HyperLog.v(TAG, "Ignoring unexpected value " + key);
                }
        }
    }

    private void checkLocationPermissionAndAccessAndBatteryOptimizationAndStartWorker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.d(TAG, "Location permission not yet granted, asking user ...");
            FragmentManager fm = getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                if (ok) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }, R.string.permission_dialog_title, R.string.permission_dialog_message);
            fragment.show(fm, null);
        } else {
            checkLocationAccessAndBatteryOptimizationAndStartWorker();
        }
    }

    private void checkLocationAccessAndBatteryOptimizationAndStartWorker() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && (locationManager == null || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))) {
            HyperLog.d(TAG, "Location service not yet enabled, asking user ...");
            FragmentManager fm = getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                if (ok) {
                    startActivityForResult(new Intent(ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE);
                }
            }, R.string.location_dialog_title, R.string.location_dialog_message);
            fragment.show(fm, null);
        } else {
            checkBatteryOptimizationAndStartWorker();
        }
    }

    private void checkBatteryOptimizationAndStartWorker() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null
                && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            HyperLog.d(TAG, "Battery optimization not yet disabled, asking user ...");
            FragmentManager fm = getSupportFragmentManager();

            // this app should fall under "task automation app" in
            // https://developer.android.com/training/monitoring-device-state/doze-standby.html#whitelisting-cases
            @SuppressLint("BatteryLife")
            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                if (ok) {
                    Uri packageUri = Uri.fromParts("package", getPackageName(), null);
                    startActivityForResult(
                            new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri), BATTERY_OPTIMIZATION_REQUEST_CODE);
                }
            }, R.string.battery_optimization_dialog_title, R.string.battery_optimization_dialog_message);
            fragment.show(fm, null);
        } else {
            new Publisher(this).scheduleNow();
        }
    }
}
