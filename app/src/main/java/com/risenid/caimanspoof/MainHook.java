/*
 * WearableSpoof
 * Copyright (C) 2023 Simon1511
 * CaimanSpoof
 * Copyright (C) 2024 RisenID
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.risenid.caimanspoof;

import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("caimanSpoof: Hooking into: " + lpparam.packageName);

        // Set build properties
        XposedHelpers.setStaticObjectField(Build.class, "MANUFACTURER", "Google");
        XposedHelpers.setStaticObjectField(Build.class, "BRAND", "google");
        XposedHelpers.setStaticObjectField(Build.class, "DEVICE", "mustang");
        XposedHelpers.setStaticObjectField(Build.class, "TAGS", "release-keys");
        XposedHelpers.setStaticObjectField(Build.class, "TYPE", "user");
        XposedHelpers.setStaticObjectField(Build.class, "PRODUCT", "mustang");
        XposedHelpers.setStaticObjectField(Build.class, "MODEL", "Pixel 10 Pro");
        XposedHelpers.setStaticObjectField(Build.class, "BOARD", "mustang");
        XposedHelpers.setStaticObjectField(Build.class, "ID", "AP4A.250805.002");
        XposedHelpers.setStaticObjectField(Build.class, "FINGERPRINT",
                "google/mustang/mustang:16/AP4A.250805.002/12701944:user/release-keys");
        XposedHelpers.setStaticObjectField(Build.class, "bootimage.id", "AP4A.250805.002");
        XposedHelpers.setStaticObjectField(Build.class, "bootimage.fingerprint",
                "google/mustang/mustang:16/AP4A.250805.002/12701944:user/release-keys");
        XposedHelpers.setStaticObjectField(Build.class, "vendor.id", "AP4A.250805.002");
        XposedHelpers.setStaticObjectField(Build.class, "vendor.fingerprint",
                "google/mustang/mustang:15/AP4A.250805.002/12701944:user/release-keys");

        try {
            System.setProperty("ro.product.vendor.brand", "google");
            System.setProperty("ro.product.vendor.device", "mustang");
            System.setProperty("ro.product.vendor.manufacturer", "google");
            System.setProperty("ro.product.vendor.model", "mustang");
            System.setProperty("ro.product.vendor.name", "mustang");
        } catch (Exception e) {
            XposedBridge.log("Failed to set vendor properties: " + e.getMessage());
        }
    }
}