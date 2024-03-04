package com.github.passdrive;

import com.github.passdrive.usbDetector.manager.DetectTaskSchduler;
import com.github.passdrive.utils.configPopulator;

// Workflow
// Thread1: Detector
public class App {
    // populate configurations if exists
    public static void main(String[] args) {
        // populate configurations
        configPopulator.populate();
        
        // USB Detection phase
        DetectTaskSchduler detectTaskSchduler = new DetectTaskSchduler();
        detectTaskSchduler.start();
    }
}

// TODO: test + exception handle