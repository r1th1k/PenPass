package com.github.passdrive.usbDetector.PlatformDetectorTasks;

import static com.github.passdrive.utils.constants.UsbConstants.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.github.passdrive.usbDetector.UsbDevice;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.interfaces.UsbDetector;

public class WindowsUsbDetector implements UsbDetector {
    private String detectedVolume = "";
    private UsbDevice detectedDevice = null;
    // Command
    private final String COMMAND = WMIC_PATH + " logicaldisk where drivetype=" + WIN_DEVICE_TPE
            + " get drivetype,deviceid /format:csv";

    private UsbDevice parseDevices(InputStream devices) {
        BufferedReader temp = new BufferedReader(new InputStreamReader(devices));
        String line;
        try {
            while ((line = temp.readLine()) != null) {
                // Skip the first line
                if (line.contains("Node,")) {
                    continue;
                }

                // Capture USB device
                else if (line.contains("" + WIN_DEVICE_TPE)) {
                    String[] device = line.split(",");
                    return new UsbDevice(true, device[1].trim());
                }
            }
        } catch (Exception e) {
            return new UsbDevice(false, null);
        }
        return new UsbDevice(false, null);
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
                this.detectedVolume = result.getDeviceVolume();
                this.detectedDevice = result;
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

