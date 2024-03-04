package com.github.passdrive.usbDetector.manager.platform.tasks;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.LinuxUsbDetector;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.interfaces.UsbDetector;
import com.github.passdrive.usbDetector.manager.DetectTaskSchduler;
import com.github.passdrive.usbDetector.manager.platform.interfaces.Task;

/**
 * Linux task
 */
public class LinTask extends Task {
    DetectTaskSchduler scheduler;

    public LinTask(DetectTaskSchduler detectTaskSchduler) {
        this.scheduler = detectTaskSchduler;
    }

    @Override
    public void run() {
        UsbDetector detector = new LinuxUsbDetector();
        try {
            if (detector.detect()) {
                System.out.println("USB Detected: " + detector.getDetectedDevice());

                // Environment set
                EnvironmentImpl.setEnvironmentMap("detected", Boolean.TRUE);
                EnvironmentImpl.setEnvironmentMap("volume", detector.getDetectedVolume());
                EnvironmentImpl.setEnvironmentMap("usbdevice", detector.getDetectedDevice());
                scheduler.stop();
            } else {
                System.out.println("Waiting for USB device...");
            }
        } catch (InterruptedException e) {
            // TODO: fallback
            System.out.println("Try again Later!");
            System.exit(1);
        }
    }

}
