package com.github.passdrive.protector.configProtection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.usbDetector.UsbDevice;

/**
 * When a new Pendrive get inserted need to configure
 * master password. Write to the path
 * Path: Volume/.pendrive
 */

public class configurePendrive {
    public static Boolean configure(UsbDevice usbDrive, String hashedPassword) {
        if (usbDrive.getIsDetected()) {
            
            // Get Environment root to store config file
            String root = "";
            EnvironmentImpl.getEnvironmentMap("root");

            File protect = new File(
                    "" + usbDrive.getDeviceVolume() + File.separator + root + ".protected");
            
            // Create .protected write master password
            try {
                protect.createNewFile();
                FileOutputStream fio = new FileOutputStream(protect);

                fio.write(hashedPassword.getBytes());
                fio.close();

                return true;
            } catch (IOException io) {
                // Fallback
                return false;
            }

        }

        return false;
    }
}
