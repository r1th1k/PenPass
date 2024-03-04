package com.github.passdrive.usbDetector.PlatformDetectorTasks.interfaces;

import com.github.passdrive.usbDetector.UsbDevice;

/**
 * Usb Detector Interface
 * <windows>
 * <mac>
 * <linux>
 */

 public interface UsbDetector {
    Boolean detect() throws InterruptedException;
    String getDetectedVolume();
    UsbDevice getDetectedDevice();
}