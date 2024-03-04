package com.github.passdrive.protector.configProtection;

import java.io.File;
import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.usbDetector.UsbDevice;

/**
 * If already passkey pendrive
 * -> true
 * else
 * -> false
 */

public class detectConfiguration {
    public static Boolean detect(UsbDevice usbDrive) {
        if (usbDrive.getIsDetected()) {
            /**
             * 
             * volume/.protected file will be generated if
             * it already been attached and configured
             * else create one -> configurePendrive
             * 
             */
            String root = "";
            EnvironmentImpl.getEnvironmentMap("root");

            /**
             * Can change Root path of .protected
             */

            File protect = new File(
                    "" + usbDrive.getDeviceVolume() + File.separator + root + ".protected");
            if (protect.exists()) {
                return true;
            }
        }
        return false;
    }
}
