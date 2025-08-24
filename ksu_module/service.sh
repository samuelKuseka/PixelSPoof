#!/system/bin/sh
# Pixel 10 Pro XL Spoofing Module for KernelSU
# This module modifies system properties to make the device appear as Pixel 10 Pro XL

MODDIR=${0%/*}
LOG_FILE="/data/local/tmp/pixel_spoof.log"

log_print() {
    echo "[$(date)] $1" >> $LOG_FILE
    echo "$1"
}

log_print "Starting Pixel 10 Pro XL spoofing..."

# Wait for system to be ready
sleep 5

# Reset system properties to Pixel 10 Pro XL
resetprop_if_exist() {
    local prop_name="$1"
    local prop_value="$2"
    
    if [ -n "$(getprop $prop_name)" ]; then
        resetprop "$prop_name" "$prop_value"
        log_print "Set $prop_name = $prop_value"
    fi
}

# Core device identification
resetprop_if_exist "ro.product.manufacturer" "Google"
resetprop_if_exist "ro.product.brand" "google" 
resetprop_if_exist "ro.product.name" "mustang_beta"
resetprop_if_exist "ro.product.device" "mustang"
resetprop_if_exist "ro.product.model" "Pixel 10 Pro XL"
resetprop_if_exist "ro.product.product.manufacturer" "Google"
resetprop_if_exist "ro.product.product.brand" "google"
resetprop_if_exist "ro.product.product.name" "mustang_beta"
resetprop_if_exist "ro.product.product.device" "mustang"
resetprop_if_exist "ro.product.product.model" "Pixel 10 Pro XL"

# Build properties
resetprop_if_exist "ro.build.product" "mustang"
resetprop_if_exist "ro.build.device" "mustang"
resetprop_if_exist "ro.build.fingerprint" "google/mustang_beta/mustang:16/BP41.250725.006/12701944:user/release-keys"
resetprop_if_exist "ro.build.description" "mustang_beta-user 16 BP41.250725.006 12701944 release-keys"
resetprop_if_exist "ro.build.id" "BP41.250725.006"
resetprop_if_exist "ro.build.display.id" "BP41.250725.006"
resetprop_if_exist "ro.build.version.release" "16"
resetprop_if_exist "ro.build.version.release_or_codename" "16"
resetprop_if_exist "ro.build.version.sdk" "36"
resetprop_if_exist "ro.build.version.incremental" "12701944"
resetprop_if_exist "ro.build.version.security_patch" "2025-08-05"
resetprop_if_exist "ro.build.tags" "release-keys"
resetprop_if_exist "ro.build.type" "user"

# Hardware properties for Pixel 10 Pro XL (Tensor G5)
resetprop_if_exist "ro.hardware" "mustang"
resetprop_if_exist "ro.board.platform" "gs305"
resetprop_if_exist "ro.hardware.chipname" "gs305"
resetprop_if_exist "ro.soc.manufacturer" "Google"
resetprop_if_exist "ro.soc.model" "Tensor G5"

# Bootloader and security
resetprop_if_exist "ro.bootloader" "mustang-1.0-12701944"
resetprop_if_exist "ro.boot.hardware" "mustang"
resetprop_if_exist "ro.boot.hardware.platform" "gs305"
resetprop_if_exist "ro.boot.hardware.revision" "PROTO1.0"

# Additional Pixel-specific properties
resetprop_if_exist "ro.opa.eligible_device" "true"
resetprop_if_exist "ro.com.google.ime.kb_pad_port_b" "1"
resetprop_if_exist "ro.storage_manager.enabled" "true"
resetprop_if_exist "ro.atrace.core.services" "com.google.android.gms,com.google.android.gms.ui,com.google.android.gms.persistent"

# AICore properties - CRUCIAL for Pixel AI features
resetprop_if_exist "ro.config.aicore_enabled" "true"
resetprop_if_exist "persist.vendor.aicore.enabled" "1"
resetprop_if_exist "ro.vendor.aicore.version" "16.0"
resetprop_if_exist "ro.system.aicore.enabled" "true"
resetprop_if_exist "ro.config.ai_core_available" "true"

# Pixel exclusive features
resetprop_if_exist "ro.vendor.audio.sdk.fluencetype" "fluence"
resetprop_if_exist "ro.config.face_unlock_service" "true"
resetprop_if_exist "ro.com.google.lens.oem" "true"

log_print "Pixel 10 Pro XL spoofing completed successfully!"

# Verify some key properties
log_print "Verification:"
log_print "Manufacturer: $(getprop ro.product.manufacturer)"
log_print "Model: $(getprop ro.product.model)" 
log_print "Brand: $(getprop ro.product.brand)"
log_print "Fingerprint: $(getprop ro.build.fingerprint)"
