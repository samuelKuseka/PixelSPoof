# PixelSpoof

## What is PixelSpoof?
PixelSpoof is an advanced Xposed module for LSPosed that allows you to spoof device-specific properties to various Pixel devices, including the latest Pixel 10 Pro XL with Android 16 QPR2. Get access to Pixel-exclusive features on any Android device!

## ✨ Features
- **Multiple Device Profiles**: Switch between Pixel 10 Pro XL, Pixel 10 Pro, Pixel 9 Pro XL, and more
- **Dynamic Updates**: Device profiles automatically update from GitHub (no app update needed)
- **Configuration UI**: Easy-to-use settings app to select your preferred Pixel device
- **Latest Android 16**: Supports the newest Android 16 QPR2 builds
- **Per-App Spoofing**: Select which apps should see the spoofed device properties

## How to install
### Prerequisites
To use this module you must have one of the following (latest versions):
- [Magisk](https://github.com/topjohnwu/Magisk) with Zygisk enabled
    - IMPORTANT: DO NOT add apps that you want to spoof to Magisk's denyList as that will break the module.
- [KernelSU](https://github.com/tiann/KernelSU) with [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) module installed
- [APatch](https://github.com/bmax121/APatch) with [ZygiskNext MOD](https://github.com/Yervant7/ZygiskNext) module installed
You must also have [LSPosed](https://github.com/mywalkb/LSPosed_mod) installed

### Installation
- Download the latest APK of PixelSpoof from the [releases section](https://github.com/kashi/PixelSpoof/releases) and install it like any normal APK.
- Now open the LSPosed Manager and go to "Modules".
- PixelSpoof should now appear in that list.
- Click on PixelSpoof and enable the module by flipping the switch at the top that says "Enable module".
- Next, tick all the apps that you want to spoof details for and reboot your phone afterwards.
- Once rebooted, you can open the PixelSpoof app to configure which device profile to use.

## Configuration
1. **Open PixelSpoof app** from your app drawer
2. **Select device profile** from the dropdown (Pixel 10 Pro XL, Pixel 10 Pro, etc.)
3. **Click Apply** and reboot your device
4. **Refresh profiles** anytime to get the latest device properties from GitHub

## FAQ and issues
- **Q: How do I change which Pixel device to spoof?**  
  A: Open the PixelSpoof app and select a different profile from the dropdown.

- **Q: Do I need to update the app for new device profiles?**  
  A: No! Device profiles automatically update from GitHub daily.

- **Q: Which apps work best with spoofing?**  
  A: Camera apps, Google apps, and apps with Pixel-exclusive features work great.

## Device Profiles Available
- Pixel 10 Pro XL (mustang) - Android 16 QPR2
- Pixel 10 Pro (frankel) - Android 16 QPR2  
- Pixel 10 (blazer) - Android 16 QPR2
- Pixel 9 Pro XL (caiman) - Android 16 QPR1
- Pixel 9 Pro (komodo) - Android 16 QPR1
- Pixel 8 Pro (husky) - Android 15

Profiles are automatically updated with the latest build numbers and fingerprints!