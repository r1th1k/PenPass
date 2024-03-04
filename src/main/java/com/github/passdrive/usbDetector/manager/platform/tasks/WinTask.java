package com.github.passdrive.usbDetector.manager.platform.tasks;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.usbDetector.PlatformDetectorTasks.WindowsUsbDetector;
import com.github.passdrive.usbDetector.manager.DetectTaskSchduler;
import com.github.passdrive.usbDetector.manager.platform.interfaces.Task;

// WIN Task
public class WinTask extends Task {
    DetectTaskSchduler scheduler;

    public WinTask(DetectTaskSchduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        WindowsUsbDetector detector = new WindowsUsbDetector();
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