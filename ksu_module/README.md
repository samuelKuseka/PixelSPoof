# Pixel 10 Pro XL KernelSU Spoofing Module

This KernelSU module modifies system properties to make your device appear as a Google Pixel 10 Pro XL with Tensor G5 processor running Android 16.

## Features
- ✅ Complete system-level property spoofing
- ✅ Google Play Store certification fix
- ✅ Pixel exclusive features enabled
- ✅ Android 16 (API 36) spoofing
- ✅ Tensor G5 hardware spoofing
- ✅ Works with KSU-Next + SusFS

## Installation
1. Copy this module folder to your device
2. Install via KernelSU Manager
3. Reboot device
4. Check `/data/local/tmp/pixel_spoof.log` for status

## What gets spoofed
- Device manufacturer: Google
- Device model: Pixel 10 Pro XL  
- Device codename: mustang
- Android version: 16 (API 36)
- SoC: Google Tensor G5
- Build fingerprint: Pixel 10 Pro XL beta
- Security patch: 2025-08-05

## Verification
After reboot, check:
```bash
getprop ro.product.manufacturer  # Should show "Google"
getprop ro.product.model         # Should show "Pixel 10 Pro XL"
getprop ro.build.fingerprint     # Should show Pixel fingerprint
```

## Compatibility
- Requires KernelSU with resetprop support
- Works best with KSU-Next + SusFS
- Compatible with Android 11+

## Safety
This module only modifies cosmetic properties and does not affect:
- Bootloader security
- System integrity
- Hardware functionality
- Root detection bypasses
