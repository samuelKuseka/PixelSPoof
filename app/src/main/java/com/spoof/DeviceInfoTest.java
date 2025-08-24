package com.spoof.test;

import android.os.Build;
import android.util.Log;

public class DeviceInfoTest {
    private static final String TAG = "DeviceTest";
    
    public static void logDeviceInfo() {
        Log.i(TAG, "=== Build Information ===");
        Log.i(TAG, "MANUFACTURER: " + Build.MANUFACTURER);
        Log.i(TAG, "MODEL: " + Build.MODEL);
        Log.i(TAG, "BRAND: " + Build.BRAND);
        Log.i(TAG, "PRODUCT: " + Build.PRODUCT);
        Log.i(TAG, "DEVICE: " + Build.DEVICE);
        Log.i(TAG, "BOARD: " + Build.BOARD);
        Log.i(TAG, "HARDWARE: " + Build.HARDWARE);
        Log.i(TAG, "FINGERPRINT: " + Build.FINGERPRINT);
        Log.i(TAG, "VERSION.RELEASE: " + Build.VERSION.RELEASE);
        Log.i(TAG, "VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        Log.i(TAG, "=== End Build Info ===");
    }
}
