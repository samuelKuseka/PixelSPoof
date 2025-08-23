# 🏗️ PixelSpoof Build Instructions

## Prerequisites
- Android SDK installed (via Android Studio or command line tools)
- Java 11+ installed
- Git for repository management

## Local Building

### 1. Configure Android SDK
Update `local.properties` with your SDK path:
```properties
sdk.dir=C:\\Users\\Samuel\\AppData\\Local\\Android\\Sdk
```

### 2. Build Debug APK
```bash
# Windows PowerShell
.\gradlew assembleDebug

# Windows Command Prompt  
gradlew.bat assembleDebug
```

### 3. Build Release APK (Signed)
```bash
.\gradlew assembleRelease
```

### 4. Find Built APK
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Installation on Device

### 1. Prerequisites
- Rooted Android device
- LSPosed framework installed
- Developer options enabled
- USB debugging enabled

### 2. Install APK
```bash
adb install app-debug.apk
```

### 3. Activate in LSPosed
1. Open LSPosed Manager
2. Go to **Modules** tab
3. Enable **PixelSpoof**
4. Select target apps (usually system apps for device spoofing)
5. Reboot device

### 4. Configure Module
1. Open PixelSpoof app
2. Select desired device profile
3. Enable features as needed
4. Apply configuration

## GitHub Actions (Cloud Building)

If you prefer cloud building without local Android SDK:

### 1. Setup Repository Secrets
- `KEYSTORE_FILE`: Base64 encoded keystore
- `KEYSTORE_PASSWORD`: Keystore password  
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

### 2. Push to GitHub
GitHub Actions will automatically build and create releases.

## Troubleshooting

### Build Errors
- Ensure Android SDK path is correct in `local.properties`
- Check Java version: `java -version` (needs 11+)
- Clean build: `.\gradlew clean`

### Runtime Issues
- Verify LSPosed is working
- Check Xposed logs for errors
- Ensure target apps are selected in LSPosed

### Configuration Updates
- Module automatically fetches latest profiles from GitHub
- Manual refresh available in app settings
- Fallback to local profiles if network unavailable

## Security Notes
- Release builds are signed and obfuscated
- Debug builds are for testing only
- Keep keystore secure for release builds
