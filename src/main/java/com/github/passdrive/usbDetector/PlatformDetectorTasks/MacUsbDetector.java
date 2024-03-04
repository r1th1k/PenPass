package com.github.passdrive.usbDetector.PlatformDetectorTasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.github.passdrive.usbDetector.UsbDevice;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.interfaces.UsbDetector;

public class MacUsbDetector implements UsbDetector {
    private String detectedDevice = "";
    private UsbDevice detectedUsb = null;
    // Command
    private final String COMMAND = "diskutil list";

    private UsbDevice parseDevices(InputStream devices) {
        BufferedReader temp = new BufferedReader(new InputStreamReader(devices));
        String line;
        try {
            while ((line = temp.readLine()) != null) {
                // Find Usb Device
                if ( line.contains("external") && line.contains("physical") ) {
                    break;
                }
            }

            int volumeIDX = -1;
            int limitIDX = -1;
            line = temp.readLine();
            if (line.contains("NAME"))
                volumeIDX = line.indexOf("NAME");
            if (line.contains("SIZE"))
                limitIDX = line.indexOf("SIZE");

            // Found line
            while (!(line = temp.readLine()).contains("1")) {
            }
            // Line will contain Actual Usb Device
            String volumeStr = line.substring(volumeIDX, limitIDX);
            return new UsbDevice(true, volumeStr.trim());
        } catch (Exception e) {
            return new UsbDevice(false, null);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Boolean detect() throws InterruptedException {
        InputStream devices = null;
        UsbDevice result = null;
        // Detect USB
        System.out.println("Detecting USB");
        try {
            devices = Runtime.getRuntime().exec(COMMAND).getInputStream();
            if (devices != null) {
                result = parseDevices(new BufferedInputStream(devices));
            }

            // Return the detected USB
            if (result.getIsDetected()) {
                detectedDevice = result.getDeviceVolume();
                this.detectedUsb = result;
                return true;
            }

        } catch (IOException e) {
            return false;
        }
        return false;
    }

    @Override
    public String getDetectedVolume() {
        return this.detectedDevice;
    }

    @Override
    public UsbDevice getDetectedDevice() {
        return this.detectedUsb;
    }

    
}
