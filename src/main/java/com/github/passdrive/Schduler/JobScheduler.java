package com.github.passdrive.Schduler;

import java.util.Properties;

public interface JobScheduler extends Runnable {
    void schedule(Properties props);
    int getStatus(int jobid); 
}
