package com.adg.sync.ssh.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by adg on 10/06/2015.
 */
public class Logger {

    public static final String FILE_MESSAGE = "SyncSSH.log";
    public static final String FILE_ERROR = "SyncSSH.err";

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss dd MMMM yyyy");

    private static String folder = "";
    private static int errorCount = 0;

    public static String getFolder() {
        return folder;
    }

    public static int getErrorCount() {
        return errorCount;
    }

    public static void resetErrorCount() {
        errorCount = 0;
    }

    public static void setFolder(String folder) {
        Logger.folder = folder + "/";
    }

    public static void logMessage(String message) {
        log(message, folder + FILE_MESSAGE);
    }

    public static void logMessage(String header, String message) {
        logMessage(header + " === " + message);
    }

    public static void logError(String message) {
        errorCount++;
        log(message, folder + FILE_ERROR);
    }

    public static void logError(Exception exception) {
        errorCount++;
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder + FILE_ERROR, true)))) {
            out.println();
            out.append("Log Entry : ").append(SDF.format(new Date()));
            out.append(" :");
            exception.printStackTrace(out);
            out.append("-------------------------------");
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message, String file) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            out.println();
            out.append("Log Entry : ").append(SDF.format(new Date()));
            out.append(" :").append(message);
            out.println();
            out.append("-------------------------------");
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
