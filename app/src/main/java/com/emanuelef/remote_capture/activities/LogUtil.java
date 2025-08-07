package com.emanuelef.remote_capture.activities;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static final String LOG_PATH = "/storage/emulated/0/log.txt";

    public static void logToFile(String msg) {
        try {
            FileWriter writer = new FileWriter(LOG_PATH, true);
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            writer.write("[" + time + "] " + msg + "\n");
            writer.close();
        } catch (IOException e) {
            // silent
        }
    }
}


