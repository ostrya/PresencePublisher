package org.ostrya.presencepublisher.mqtt.context.device;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.mqtt.context.MessageContext;

public class DeviceNameProvider {
    private final Context applicationContext;

    public DeviceNameProvider(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String getDeviceName() {
        String deviceName = getDeviceNameOrNull();
        if (deviceName == null) {
            return MessageContext.UNKNOWN;
        } else {
            return deviceName;
        }
    }

    @Nullable
    private String getDeviceNameOrNull() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            String deviceName =
                    Settings.Global.getString(
                            applicationContext.getContentResolver(), Settings.Global.DEVICE_NAME);
            if (deviceName != null) {
                return deviceName;
            }
        }
        BluetoothManager bluetoothManager =
                (BluetoothManager) applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            return bluetoothManager.getAdapter().getName();
        }
        return null;
    }
}
