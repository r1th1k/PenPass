package com.github.passdrive.usbDetector.PlatformDetectorTasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.github.passdrive.usbDetector.UsbDevice;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.interfaces.UsbDetector;

/**
 * Returns mounted path from root
 */

public class LinuxUsbDetector implements UsbDetector {
    private String detectedVolume = "";
    private UsbDevice detectedDevice = null;
    // Command
    private final String COMMAND = "df -h";

    private UsbDevice parseDevices(InputStream devices) {
        BufferedReader temp = new BufferedReader(new InputStreamReader(devices));
        String line;

        try {
            int mountIDX = -1;
            // Mount point index
            if ((line = temp.readLine()) != null && line.contains("Mounted on")) {
                mountIDX = line.indexOf("Mounted on", 0);
            }

            while ((line = temp.readLine()) != null) {
                // Find Usb Device
                if (((String) line.subSequence(mountIDX, line.length())).startsWith("/Volumes")) {
                    File check = new File(line.substring(mountIDX, line.length()).trim());
                    if (check.isDirectory() && check.canRead() && check.canWrite())
                        return new UsbDevice(true, check.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            return new UsbDevice(false, null);
        }
        return new UsbDevice(false, null);
    }

    @SuppressWarnings("deprecation")
    @Override
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
                detectedVolume = result.getDeviceVolume();
                detectedDevice = result;
                return true;
            }

        } catch (IOException e) {
            return false;
        }
        return false;
    }

    @Override
    public UsbDevice getDetectedDevice() {
        return this.detectedDevice;
    }

    @Override
    public String getDetectedVolume() {
        return this.detectedVolume;
    }
}
