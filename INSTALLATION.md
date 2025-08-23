# PixelSpoof v2.0 - Installation Guide

## 📱 **Installation Requirements**

### Prerequisites
- ✅ **Android Device** (API 29+ / Android 10+)
- ✅ **Root Access** (Magisk recommended)
- ✅ **LSPosed Framework** installed and active
- ✅ **Internet Connection** for profile updates

### System Compatibility
- **Minimum Android**: 10 (API 29)
- **Target Android**: 14 (API 34)
- **Architecture**: All (arm64-v8a, armeabi-v7a, x86, x86_64)
- **LSPosed**: v1.8.6 or higher

## 🚀 **Installation Steps**

### Step 1: Download APK
- **Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Size**: ~21.6 MB
- **Package**: `com.kashi.caimanspoof`

### Step 2: Install APK
```bash
adb install app-debug.apk
# OR install manually via file manager
```

### Step 3: Enable in LSPosed
1. Open **LSPosed Manager**
2. Go to **Modules**
3. Enable **PixelSpoof**
4. Configure **Scope** (recommend system-wide for maximum compatibility)
5. **Reboot device**

### Step 4: Configure Module
1. Open **PixelSpoof** app from launcher
2. Select desired **Device Profile**
3. Enable **Stealth Mode** (recommended)
4. Enable **Auto Update** for latest profiles
5. Tap **Apply Configuration**

## 🔧 **Configuration Options**

### Device Profiles Available
- **Pixel 10 Pro XL** - Android 16 QPR2 Beta
- **Pixel 9 Pro** - Android 15 QPR1 Beta  
- **Pixel 8 Pro** - Android 14 QPR3 Beta
- **Pixel 7 Pro** - Android 13 QPR3 Beta
- **Pixel 6 Pro** - Android 12L Beta

### Stealth Features
- ✅ **Framework Detection Bypass** - Hides Xposed/LSPosed
- ✅ **Root Concealment** - Masks root access
- ✅ **SafetyNet Evasion** - Bypasses integrity checks
- ✅ **Play Protect Bypass** - Avoids Google scanning
- ✅ **Stealth Logging** - Hidden debug output

### Auto-Update System
- **Source**: GitHub repository
- **Frequency**: Daily checks
- **URL**: `https://raw.githubusercontent.com/samuelKuseka/PixelSpoof/main/device_profiles.json`
- **Fallback**: Local profiles if network fails

## ✅ **Verification**

### Check Module Status
1. Open **PixelSpoof** app
2. Verify **"Module Active"** status
3. Check **selected profile** is applied
4. Review **last update** timestamp

### Test Device Spoofing
```bash
# Check spoofed properties
adb shell getprop ro.build.fingerprint
adb shell getprop ro.product.model
adb shell getprop ro.product.manufacturer

# Should show selected Pixel device info
```

### Verify Stealth Mode
- Run **SafetyNet** check (should pass)
- Test **banking apps** (should work)
- Check **Play Store certification**

## 🚨 **Troubleshooting**

### Module Not Working
1. **Reboot** after enabling in LSPosed
2. Check **LSPosed logs** for errors
3. Verify **scope configuration** includes target apps
4. Ensure **device compatibility** (Android 10+)

### Profile Update Failures
1. Check **internet connection**
2. Verify **GitHub repository** is accessible
3. Review **network permissions**
4. Try **manual refresh** in app

### Stealth Detection
1. **Disable other Xposed modules** temporarily
2. Check **Magisk Hide** configuration
3. Update to **latest LSPosed** version
4. Review **app-specific settings**

## 📋 **Support & Updates**

### Repository
- **GitHub**: https://github.com/samuelKuseka/PixelSpoof
- **Issues**: Report bugs via GitHub Issues
- **Releases**: Tagged versions with APKs

### Version Info
- **Current**: v2.0
- **Build**: August 23, 2025
- **Commit**: e60fcee

### Features Included
- ✅ Modern Kotlin architecture
- ✅ Jetpack Compose Material3 UI
- ✅ Dynamic GitHub configuration
- ✅ Advanced anti-detection
- ✅ Research-verified fingerprints
- ✅ Comprehensive error handling

## ⚠️ **Important Notes**

### Legal Disclaimer
- Use responsibly and comply with app Terms of Service
- Intended for development and testing purposes
- Not responsible for account suspensions

### Security Considerations
- Module runs with **system-level privileges**
- Only install from **trusted sources**
- Review **source code** before installation
- Keep **LSPosed** and **Magisk** updated

### Performance Impact
- **Minimal CPU overhead** 
- **Low memory footprint**
- **No battery drain** when idle
- **Optimized hook implementations**

---

**Success!** Your device now appears as a genuine Pixel device with advanced anti-detection features. 🎯
