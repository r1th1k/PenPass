package com.github.passdrive.usbDetector.manager.platform.tasks;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.MacUsbDetector;
import com.github.passdrive.usbDetector.manager.DetectTaskSchduler;
import com.github.passdrive.usbDetector.manager.platform.interfaces.Task;

/**
 * Mac Os task
 */

public class DarTask extends Task {
    DetectTaskSchduler scheduler;

    public DarTask(DetectTaskSchduler detectTaskSchduler) {
        this.scheduler = detectTaskSchduler;
    }

    @Override
    public void run() {
        MacUsbDetector detector = new MacUsbDetector();
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
