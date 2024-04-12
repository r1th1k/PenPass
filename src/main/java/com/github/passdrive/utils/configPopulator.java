package com.github.passdrive.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.github.passdrive.Environment.EnvironmentImpl;

public class configPopulator {
    private static final String CONFIG_FILE = "properties.config";
    public static boolean isPopulated = false;
    // Populate peoperties.config to EnvironmentImpl
    public static void populate() {
        EnvironmentImpl.setEnvironmentMap("logged", Boolean.FALSE);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(CONFIG_FILE);

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        Iterator<String> it = reader.lines().iterator();
        
        while (it.hasNext()) {
            String line = (String) it.next();
            String[] tokens = line.split("=");
            String key = tokens[0];
            String value = tokens[1];
            EnvironmentImpl.setEnvironmentMap(key, value);
        }
        configPopulator.isPopulated = true;
    }

    public static void write(String key, String value) {
        EnvironmentImpl.setEnvironmentMap(key, value);
    }
}