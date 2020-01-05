package org.ostrya.presencepublisher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
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

import java.util.Collections;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static org.ostrya.presencepublisher.Application.*;
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
    private boolean needsLocationService;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        HyperLog.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mainPagerAdapter);

        needsLocationService = ((Application) getApplication()).supportsBeacons()
                // for WiFi name resolution
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);

        checkLocationPermissionAndAccessAndBluetoothAndBatteryOptimizationAndStartWorker();

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
            checkLocationAccessAndBluetoothAndBatteryOptimizationAndStartWorker();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE) {
            // For some reason, the activity returns RESULT_CANCELED even when the service is enabled, so we don't
            // know if it was actually enabled or not. For now, we don't check again and just start the service.
            HyperLog.d(TAG, "Returning from location service with result " + resultCode + ", assuming it is running ...");
            checkBluetoothAndBatteryOptimizationAndStartWorker();
        } else if (requestCode == START_BLUETOOTH_REQUEST_CODE) {
            HyperLog.d(TAG, "Returning from bluetooth enabling with result " + resultCode);
            checkBatteryOptimizationAndStartWorker();
        } else if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
            HyperLog.d(TAG, "Returning from battery optimization with result " + resultCode);
            new Publisher(this).scheduleNow();
        }
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

    private void checkLocationPermissionAndAccessAndBluetoothAndBatteryOptimizationAndStartWorker() {
        if (needsLocationService
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.d(TAG, "Location permission not yet granted, asking user ...");
            FragmentManager fm = getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                if (ok) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                PERMISSION_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE);
                    }
                }
            }, R.string.permission_dialog_title, R.string.permission_dialog_message);
            fragment.show(fm, null);
        } else {
            checkLocationAccessAndBluetoothAndBatteryOptimizationAndStartWorker();
        }
    }

    private void checkLocationAccessAndBluetoothAndBatteryOptimizationAndStartWorker() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (needsLocationService
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && (locationManager == null || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
            checkBluetoothAndBatteryOptimizationAndStartWorker();
        }
    }

    private void checkBluetoothAndBatteryOptimizationAndStartWorker() {
        if (((Application) getApplication()).supportsBeacons()
                // make linter happy
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && !sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()).isEmpty()) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                HyperLog.w(TAG, "Unable to get bluetooth manager");
            } else {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    FragmentManager fm = getSupportFragmentManager();

                    ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                        if (ok) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, START_BLUETOOTH_REQUEST_CODE);
                        }
                    }, R.string.bluetooth_dialog_title, R.string.bluetooth_dialog_message);
                    fragment.show(fm, null);
                    return;
                }
            }
        }
        checkBatteryOptimizationAndStartWorker();
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
