package com.github.passdrive.usbDetector;

import java.io.File;

public class UsbDevice {
    private Boolean isDetected;
    private String deviceVolume;

    public UsbDevice(Boolean isDetected, String deviceVolume) {
        this.isDetected = isDetected;
        this.deviceVolume = deviceVolume;
    }

    public String getDeviceVolume() {
        return deviceVolume;
    }

    public Boolean getIsDetected() {
        return isDetected;
    }

    public void setDeviceVolume(String deviceVolume) {
        this.deviceVolume = deviceVolume;
    }

    public void setIsDetected(Boolean isDetected) {
        this.isDetected = isDetected;
    }

    public Boolean isRemoved() {
        // read deviceVolume if not exists disconnected
        if (getIsDetected()) {
            String volume = getDeviceVolume();
            
            File file = new File(volume);
            if (!file.exists()) {
                setIsDetected(false);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
