package com.github.passdrive.pendrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.Schduler.StoreJobScheduler;
import com.github.passdrive.encryptor.Encipher;
import com.github.passdrive.encryptor.Algorithms.interfaces.Algorithm;
import com.github.passdrive.protector.hashing.Hash;
import com.github.passdrive.protector.hashing.interfaces.hash;
import com.github.passdrive.usbDetector.UsbDevice;
import com.github.passdrive.utils.constants.AppConstants;
import com.google.gson.JsonObject;

/*
 * Manages stores encrypted and
 * hashed password
 * test if password is correct
 */

/*
 * env: "root"
 * Path(master): Volume/.protected
 * Path(passwords): Volume/data/passwords/domain
 */

public class PasswordManager {
    public static Boolean storeMasterPassword(UsbDevice usb, String password) {
        if ( usb.getIsDetected() ) {
            // Get Environment root to store config file
            String root = "";
            EnvironmentImpl.getEnvironmentMap("root");

            // TODO: check valid path (condition: //..)
            File protect = new File(
                    "" + usb.getDeviceVolume() + File.separator + root + ".protected");
            
            // Create .protected write master password
            try {
                protect.createNewFile();
                FileOutputStream fio = new FileOutputStream(protect);

                hash sha = Hash.getAlgorithm("SHA-256");
                fio.write( sha.doHash(password).getBytes() );
                fio.close();

                return true;
            } catch (IOException io) {
                // Fallback
                return false;
            }
        }
        return false;
    }

    public static Boolean checkMasterPassword(UsbDevice usb, String password) {
        if ( usb.getIsDetected() ) {
            // Get Environment root to store config file
            String root = "";
            EnvironmentImpl.getEnvironmentMap("root");

            File protect = new File(
                    "" + usb.getDeviceVolume() + File.separator + root + ".protected");
            
            try {
                if ( !protect.exists() ) {
                    return false;
                }

                // Read master password from .protected
                FileInputStream fio = new FileInputStream(protect);
                byte[] data = new byte[(int) protect.length()];
                fio.read(data);
                fio.close();

                hash sha = Hash.getAlgorithm("SHA-256");
                String hashedPassword = sha.doHash(password);
                return sha.checkHash(data.toString(), hashedPassword);
            } catch (IOException io) {
                // Fallback
                return false;
            }
        }
        return false;
    }

    // Site passwords
    public static Boolean storePassword(String root, UsbDevice usb, String domainPath, String subDomain, String username, String password, String destDirectory, boolean fetchFlag, int bufferID) {
        File domain = new File(
                "" + destDirectory + File.separator + "" + bufferID + "");

        try {
            // if ( !protect.exists() ) {
            //     return false;
            // }
            // Recursively create if not exists directory
            if ( !domain.exists() ) {
                domain.createNewFile();
            }

            FileOutputStream fio = new FileOutputStream(domain);
            
            Algorithm aes = Encipher.getAlgorithm("AES");
            aes.init();

            // subdomain:username:password;...
            fio.write( aes.encrypt(subDomain).getBytes() );
            fio.write( aes.encrypt(username).getBytes() );
            fio.write( aes.encrypt(password).getBytes() );
            fio.close();

            // Scheduler to write to usb device
            {
                Properties properties = new Properties();
                properties.setProperty("isUsbDetected", Boolean.toString(usb.getDeviceVolume()!=null));
                properties.setProperty("volume", usb.getDeviceVolume());
                properties.setProperty("bufferfile", domain.getAbsolutePath());
                properties.setProperty("root", root);
                properties.setProperty("domainname", domainPath);
                properties.setProperty("fetch", Boolean.toString(fetchFlag));
                properties.setProperty("operation", Integer.toString(AppConstants.WRITE));
                properties.setProperty("subdomain", subDomain);

                properties.setProperty("secret-aes", aes.getKey());
                properties.setProperty("iv-aes", aes.getIv());

                StoreJobScheduler scheduler = new StoreJobScheduler(properties);
                Thread thread = new Thread(scheduler, "Scheduler Job" + bufferID);
                thread.start();
            }

            return true;
        } catch (Exception io) {
            // Fallback
            return false;
        }
    }

    public static JsonObject getPassword(String root, UsbDevice usb, String domainPath, String subDomain, String destDirectory) {
        JsonObject pas = new JsonObject();
        pas.addProperty("username", "");
        pas.addProperty("password", "");
        if ( usb.getIsDetected() ) {
            File protect = new File(
                    "" + destDirectory + File.separator + root + ".protected");
            File passwords = new File(
                    "" + destDirectory + File.separator + root + "data");

            try {
                if ( !protect.exists() ) {
                    return null;
                }
                // Recursively create if not exists
                if ( !passwords.exists() ) {
                    return null;
                }
                File domain = new File(passwords.getAbsolutePath() + File.separator + domainPath);
                if ( !domain.exists() ) {
                    return null;
                }

                FileInputStream fio = new FileInputStream(domain);
                byte[] data = new byte[(int) domain.length()];
                fio.read(data);
                fio.close();

                Algorithm aes = Encipher.getAlgorithm("AES");
                aes.init();
                String decryptedData = aes.decrypt(data.toString());
                String[] subdomainVsPasswords = decryptedData.split(";");

                for (String password : subdomainVsPasswords) {
                    String[] temp = password.split(":");
                    if (temp[0].equals(subDomain)) {
                        pas.addProperty("username", temp[1]);
                        pas.addProperty("password", temp[2]);
                        break;
                    }
                }

            } catch (Exception io) {
                // Fallback
            }
        }
        return pas;
    }

    public static Boolean removePassword(String root, UsbDevice usb, String domainPath, String subDomain, String destDirectory, boolean fetchFlag, int bufferID) {
        return storePassword(root, usb, domainPath, subDomain, "", "", destDirectory, fetchFlag, bufferID);
    }

    public static Boolean updatePassword(String root, UsbDevice usb, String domainPath, String subDomain, String username, String password, String destDirectory, boolean fetchFlag, int bufferID) {
        return storePassword(root, usb, domainPath, subDomain, username, password, destDirectory, fetchFlag, bufferID);
    }

}

// TODO: test