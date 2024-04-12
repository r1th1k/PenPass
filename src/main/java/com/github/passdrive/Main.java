package com.github.passdrive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import com.github.passdrive.Environment.EnvironmentImpl;
import com.github.passdrive.pendrive.PasswordManager;
import com.github.passdrive.protector.configProtection.configurePendrive;
import com.github.passdrive.protector.configProtection.detectConfiguration;
import com.github.passdrive.usbDetector.UsbDevice;
import com.github.passdrive.usbDetector.manager.DetectTaskSchduler;
import com.github.passdrive.utils.Result;
import com.github.passdrive.utils.configPopulator;
import com.github.passdrive.utils.constants.AppConstants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Thread2: Manager
// Thread3: Chrome + Extension
public class Main {
    // Chrome extention native app host
    // entry point
    public static void main(String[] args) throws IOException {
        // Initiate Detector thread
        // I donno if this works? lets try
        if ( !configPopulator.isPopulated ) {
            configPopulator.populate();
        }

        // if ( EnvironmentImpl.getEnvironmentMap("detected") == null ) {
        //     EnvironmentImpl.setEnvironmentMap("detected", Boolean.FALSE);

        //     // Start Detector
        //     DetectTaskSchduler detectTaskSchduler = new DetectTaskSchduler();
        //     detectTaskSchduler.start();

        //     while (!(detectTaskSchduler.getDetectedDevice() == null)) {
        //         EnvironmentImpl.setEnvironmentMap("detected", Boolean.TRUE);
        //     }
        // }

        // TODO: check if the while loop is necessary
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Boolean isDetected = (Boolean) EnvironmentImpl.getEnvironmentMap("detected");
        Boolean isLogged = (Boolean) EnvironmentImpl.getEnvironmentMap("logged");
        String root = (String) EnvironmentImpl.getEnvironmentMap("root");

        if (isDetected && !isLogged) {
            UsbDevice usb = (UsbDevice) EnvironmentImpl.getEnvironmentMap("usbdevice");
            // Read message from Chrome
            String input = reader.readLine();

            // Parse JSON message
            JsonElement jsonElement = JsonParser.parseString(input);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // data: {"type": "", "data": }}
            String requestType = jsonObject.get("type").getAsString();

            if ("POST".equals(requestType.split("_")[0])) {
                if (requestType.contains("login")) {
                    // Login
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    String masterPassword = data.get("master").getAsString();

                    // Check Configuration
                    if (detectConfiguration.detect(usb)) {
                        // Check Master Password
                        if (PasswordManager.checkMasterPassword(usb, masterPassword)) {
                            EnvironmentImpl.setEnvironmentMap("logged", Boolean.TRUE);
                            isLogged = true;
                            System.out.println(new Result(AppConstants.LOGGEDIN, "Logged in successfully").toJson()
                                    .getAsString());
                        } else {
                            System.out.println(
                                    new Result(AppConstants.UNAUTHORIZED, "Unauthorized").toJson().getAsString());
                        }
                    } else {
                        if (configurePendrive.configure(usb, masterPassword)) {
                            // Check Master Password
                            if (PasswordManager.checkMasterPassword(usb, masterPassword)) {
                                EnvironmentImpl.setEnvironmentMap("logged", Boolean.TRUE);
                                isLogged = true;
                                System.out.println(new Result(AppConstants.LOGGEDIN, "Logged in successfully")
                                        .toJson().getAsString());
                            } else {
                                System.out.println(
                                        new Result(AppConstants.UNAUTHORIZED, "Unauthorized").toJson().getAsString());
                            }
                        } else {
                            System.out.println(
                                    new Result(AppConstants.FAILURE, "Configuration failed").toJson().getAsString());
                        }
                    }
                }
            }
        }

        if (!((Boolean) EnvironmentImpl.getEnvironmentMap("removed"))) {
            UsbDevice usb = (UsbDevice) EnvironmentImpl.getEnvironmentMap("usbdevice");
            if (usb.isRemoved()) {
                // TODO: Send message to Thread1 to start detection again

                EnvironmentImpl.setEnvironmentMap("removed", Boolean.TRUE);
                System.out
                        .println(new Result(AppConstants.LOGGEDOUT, "Logged out successfully").toJson().getAsString());
            }

            // Read message from Chrome
            String input = reader.readLine();

            // Parse JSON message
            JsonElement jsonElement = JsonParser.parseString(input);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // data: {"type": "", "data": }}
            String requestType = jsonObject.get("type").getAsString();

            if ("POST".equals(requestType.split("_")[0])) {
                if (requestType.contains("domain")) {
                    // Set domain password
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    String domainName = data.get("domain").getAsString();
                    String subDomainName = data.get("subdomain").getAsString();
                    String username = data.get("username").getAsString();
                    String domainPassword = data.get("password").getAsString();

                    if (isDetected && isLogged) {
                        if (PasswordManager.storePassword(root, usb, domainName, subDomainName, username, domainPassword, (String)EnvironmentImpl.getEnvironmentMap("bufferdir"), true, new Random().nextInt())) {
                            System.out.println(new Result(AppConstants.SUCCESS, "Domain password set successfully")
                                    .toJson().getAsString());
                        } else {
                            System.out.println(
                                    new Result(AppConstants.FAILURE, "Domain password set failed").toJson().getAsString());
                        }
                    } else {
                        if (PasswordManager.storePassword(root, usb, domainName, subDomainName, username, domainPassword, (String)EnvironmentImpl.getEnvironmentMap("bufferdir"), true, new Random().nextInt())) {
                            System.out.println(new Result(AppConstants.SUCCESS, "Domain password set successfully")
                                    .toJson().getAsString());
                        } else {
                            System.out.println(
                                    new Result(AppConstants.FAILURE, "Domain password set failed").toJson().getAsString());
                        }
                    }
                }
            }

            if ("GET".equals(requestType.split("_")[0])) {
                if (requestType.contains("domain")) {
                    // Get domain password
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    String domainName = data.get("domain").getAsString();
                    String subDomainName = data.get("subdomain").getAsString();

                    JsonObject domainPassword = PasswordManager.getPassword(root, usb, domainName, subDomainName, usb.getDeviceVolume());
                    if (domainPassword != null) {
                        JsonObject response = new JsonObject();
                        response.add("data", domainPassword);
                        System.out.println(
                                new Result(AppConstants.SUCCESS, "success", response).toJson().getAsString());
                    } else {
                        System.out.println(
                                new Result(AppConstants.NOT_FOUND, "Domain password not found").toJson().getAsString());
                    }

                }
            }

            if ("DELETE".equals(requestType.split("_")[0])) {
                if (requestType.contains("domain")) {
                    // Delete domain password
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    String domainName = data.get("domain").getAsString();
                    String subDomainName = data.get("subdomain").getAsString();

                    if (PasswordManager.removePassword(root, usb, domainName, subDomainName, (String)EnvironmentImpl.getEnvironmentMap("bufferdir"), true, new Random().nextInt())) {
                        System.out.println(new Result(AppConstants.SUCCESS, "Domain password deleted successfully")
                                .toJson().getAsString());
                    } else {
                        System.out.println(new Result(AppConstants.FAILURE, "Domain password delete failed")
                                .toJson().getAsString());
                    }
                }
            }

            if ("PUT".equals(requestType.split("_")[0])) {
                if (requestType.contains("domain")) {
                    // Update domain password
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    String domainName = data.get("domain").getAsString();
                    String subDomainName = data.get("subdomain").getAsString();
                    String username = data.get("username").getAsString();
                    String domainPassword = data.get("password").getAsString();

                    if (PasswordManager.updatePassword(root, usb, domainName, subDomainName, username, domainPassword, (String)EnvironmentImpl.getEnvironmentMap("bufferdir"), true, new Random().nextInt())) {
                        System.out.println(new Result(AppConstants.SUCCESS, "Domain password updated successfully")
                                .toJson().getAsString());
                    } else {
                        System.out.println(new Result(AppConstants.FAILURE, "Domain password update failed")
                                .toJson().getAsString());
                    }
                }
            }
        }
    }
}