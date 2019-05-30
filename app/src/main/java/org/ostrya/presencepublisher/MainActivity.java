package org.ostrya.presencepublisher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import org.ostrya.presencepublisher.ui.MainPagerAdapter;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_REQUEST_CODE = 2;

    private MainPagerAdapter mainPagerAdapter;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mainPagerAdapter);

        checkLocationPermissionAndAccessAndStartService();
        Log.d(TAG, "Creating activity finished");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Successfully granted location permission");
            checkLocationAccessAndStartService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE) {
            // For some reason, the activity returns RESULT_CANCELED even when the service is enabled, so we don't
            // know if it was actually enabled or not. For now, we don't check again and just start the service.
            Log.d(TAG, "Returning from location service with result " + resultCode + ", assuming it is running ...");
            startService();
        }
    }

    private void checkLocationPermissionAndAccessAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not yet granted, asking user ...");
            FragmentManager fm = getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                if (ok) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }, R.string.permission_dialog_title, R.string.permission_dialog_message);
            fragment.show(fm, null);
        } else {
            checkLocationAccessAndStartService();
        }
    }

    private void checkLocationAccessAndStartService() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && (locationManager == null || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))) {
            Log.d(TAG, "Location service not yet enabled, asking user ...");
            FragmentManager fm = getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(ok -> {
                if (ok) {
                    startActivityForResult(new Intent(ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE);
                }
            }, R.string.location_dialog_title, R.string.location_dialog_message);
            fragment.show(fm, null);
        } else {
            startService();
        }
    }

    private void startService() {
        Log.d(TAG, "Starting service ...");
        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }
    }
}
