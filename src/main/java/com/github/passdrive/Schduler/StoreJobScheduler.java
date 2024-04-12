package com.github.passdrive.Schduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.passdrive.encryptor.Encipher;
import com.github.passdrive.encryptor.Algorithms.interfaces.Algorithm;
import com.github.passdrive.usbDetector.manager.DetectTaskSchduler;
import com.github.passdrive.utils.constants.AppConstants;

// STRICT: 
// NO DEPENDENCY ON THE ENVIRONMENT
// NO OTHER DEPENDENCY FOR SEPERATION OF CONCERNS
public class StoreJobScheduler implements JobScheduler {
    private int status = AppConstants.JOB_PENDING;
    private Properties props;
    protected Thread jobThread;

    public StoreJobScheduler(Properties props) {
        this.props = props;
    }

    @Override
    public void schedule(Properties props) {
        boolean isUsbDetected = Boolean.valueOf(props.getProperty("isUsbDetected"));
        String usbVolume = props.getProperty("volume");
        String bufferFile = props.getProperty("bufferfile");
        String root = props.getProperty("root");
        String domainname = props.getProperty("domain");
        boolean fetchFlag = Boolean.valueOf(props.getProperty("fetch"));
        int Operation = Integer.valueOf(props.getProperty("operation"));
        String subDomainName = props.getProperty("subdomain");

        String key = props.getProperty("secret-aes");
        String ivString = props.getProperty("iv-aes");

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivString));

        DetectTaskSchduler detectTaskSchduler = new DetectTaskSchduler();
        detectTaskSchduler.start("darwin");

        // Background non-blocking wait
        int i = 0;
        while (!(detectTaskSchduler.getDetectedDevice() == null) && i < AppConstants.WAIT_THREASHOLD_ROUNDS) {
            i += 1;
        }

        if (detectTaskSchduler.getDetectedDevice() == null)
            this.status = AppConstants.FAILURE;

        // Writer
        if (isUsbDetected && fetchFlag && AppConstants.WRITE == Operation) {
            File srcFile = new File(bufferFile);

            File protect = new File(
                    "" + usbVolume + File.separator + root + ".protected");
            File passwords = new File(
                    "" + usbVolume + File.separator + root + "data");

            if (!protect.exists()) {
                this.status = AppConstants.UNAUTHORIZED;
            }
            // Recursively create if not exists
            if (!passwords.exists()) {
                passwords.mkdirs();
            }

            File domain = new File(passwords.getAbsolutePath() + File.separator + domainname);
            try {
                // Read source file to get buffer
                FileInputStream tempfi = new FileInputStream(srcFile);
                byte[] buffer = tempfi.readAllBytes();
                tempfi.close();
                srcFile.delete();

                if (!domain.exists()) {
                    domain.createNewFile();
                }

                FileInputStream fi = new FileInputStream(domain);
                byte[] Buffer = fi.readAllBytes();
                fi.close();

                FileOutputStream fio = new FileOutputStream(domain);
                fio.write(Buffer);
                fio.write(';');
                fio.write(buffer);
                fio.close();
                this.status = AppConstants.SUCCESS;
            } catch (Exception e) {
                this.status = AppConstants.FAILURE;
            }
        }

        else if ( isUsbDetected && fetchFlag ) {
            // Update Scheduled write
            if ( AppConstants.UPDATE == Operation ) {
                File srcFile = new File(bufferFile);

                File protect = new File(
                    "" + usbVolume + File.separator + root + ".protected");
                File passwords = new File(
                        "" + usbVolume + File.separator + root + "data");

                if (!protect.exists()) {
                    this.status = AppConstants.UNAUTHORIZED;
                }
                // Recursively create if not exists
                if (!passwords.exists()) {
                    this.status = AppConstants.NOT_FOUND;
                    return;
                }

                File domain = new File(passwords.getAbsolutePath() + File.separator + domainname);
                try {
                    // Read source file to get buffer
                    FileInputStream tempfi = new FileInputStream(srcFile);
                    byte[] buffer = tempfi.readAllBytes();
                    tempfi.close();
                    srcFile.delete();

                    if (!domain.exists()) {
                        this.status = AppConstants.NOT_FOUND;
                        return;
                    }

                    FileInputStream fi = new FileInputStream(domain);
                    byte[] Buffer = fi.readAllBytes();
                    fi.close();

                    // "AES/CBC/PKCS5Padding"
                    Algorithm aes = Encipher.getAlgorithm("AES");
                    String inMemoryPasswords = aes.decrypt("AES/CBC/PKCS5Padding", new String(Buffer), secretKey, iv);
                    String[] passwordsArray = inMemoryPasswords.split(";");
                    for (int j = 0; j < passwordsArray.length; j++) {
                        if (passwordsArray[j].contains(subDomainName)) {
                            passwordsArray[j] = new String(buffer);
                        }
                    }
                    String updatedPasswords = String.join(";", passwordsArray);

                    FileOutputStream fio = new FileOutputStream(domain);
                    fio.write(updatedPasswords.getBytes());
                    fio.close();
                    this.status = AppConstants.SUCCESS;
                } catch (Exception e) {
                    this.status = AppConstants.FAILURE;
                }
            }

            else if ( AppConstants.DELETE == Operation ) {
                File protect = new File(
                    "" + usbVolume + File.separator + root + ".protected");
                File passwords = new File(
                        "" + usbVolume + File.separator + root + "data");

                if (!protect.exists()) {
                    this.status = AppConstants.UNAUTHORIZED;
                }
                // Recursively create if not exists
                if (!passwords.exists()) {
                    this.status = AppConstants.NOT_FOUND;
                    return;
                }

                File domain = new File(passwords.getAbsolutePath() + File.separator + domainname);
                try {
                    if (!domain.exists()) {
                        this.status = AppConstants.NOT_FOUND;
                        return;
                    }
                    
                    FileInputStream fi = new FileInputStream(domain);
                    byte[] Buffer = fi.readAllBytes();
                    fi.close();

                    Algorithm aes = Encipher.getAlgorithm("AES");
                    String inMemoryPasswords = aes.decrypt("AES/CBC/PKCS5Padding", new String(Buffer), secretKey, iv);

                    String[] passwordsArray = inMemoryPasswords.split(";");
                    for (int j = 0; j < passwordsArray.length; j++) {
                        if (passwordsArray[j].contains(subDomainName)) {
                            passwordsArray[j] = "";
                        }
                    }
                    String updatedPasswords = String.join(";", passwordsArray);

                    FileOutputStream fio = new FileOutputStream(domain);
                    fio.write(updatedPasswords.getBytes());
                    fio.close();

                    this.status = AppConstants.SUCCESS;
                } catch (Exception e) {
                    this.status = AppConstants.FAILURE;
                }
            }
        }
        this.status = AppConstants.FAILURE;
    }

    @Override
    public int getStatus(int jobid) {
        return status;
    }

    @Override
    public void run() {
        schedule(this.props);
    }
}
