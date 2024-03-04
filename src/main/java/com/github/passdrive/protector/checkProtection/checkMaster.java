package com.github.passdrive.protector.checkProtection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.usbDetector.UsbDevice;

/**
 * Check if entered password matches
 * stored encrypted password
 * Store Env: status=?(bool)
 */

public class checkMaster {
    public static Boolean check(UsbDevice usbDrive, String hashedPassword, EnvironmentImpl environment) {
        if (usbDrive.getIsDetected()) {
            /**
             * 
             * If master password matches -> Env: status=true
             * Else -> Env: status=false
             * 
             */
            String root = "";
            EnvironmentImpl.getEnvironmentMap("root");

            File protect = new File(
                    "" + usbDrive.getDeviceVolume() + File.separator + root + ".protected");

            try {
                protect.createNewFile();
                FileInputStream fin = new FileInputStream(protect);

                if (fin.readAllBytes().toString().equals(hashedPassword)) {
                    EnvironmentImpl.setEnvironmentMap("status", "true");
                    fin.close();
                    return true;
                } else {
                    EnvironmentImpl.setEnvironmentMap("status", "false");
                }
                fin.close();
                return true;
            } catch (IOException io) {
                // Fallback
                return false;
            }
        }
        return false;
    }
}
