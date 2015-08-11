package com.adg.sync.ssh.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by adg on 10/06/2015.
 *
 * A class used to read from the init file (SyncSSH.ini)
 */
public class Initializer {
    public static final String FILE_INIT = "/SyncSSH.ini";

    private static final String ACTION = "action", HOST = "host", USER = "username", PASSWORD = "password", SEP = "=";
    public static final String PATH_DOWNLOAD = ">>>", PATH_UPLOAD = "<<<", PATH_SEP = ";", PATH_EXT_SEP = ",";

    private boolean initialised = false;
    private String username = "", password = "", host = "";
    private List<ActionDescriptor> actionDescriptors = new ArrayList<>();

    /**
     * Initializes this object from the specified path.
     * @param path path to the folder that contains the SyncSSH.ini file
     */
    public void init(String path) {
        actionDescriptors.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(path + FILE_INIT))) {
            String line;
            int lineNr = 0;
            while ((line = br.readLine()) != null) {
                lineNr++;
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                int index = line.indexOf(SEP);
                if (index < 0) {
                    Logger.logError("Invalid line: " + lineNr + " (" + line + ")");
                    continue;
                }

                String name = line.substring(0, index).trim();
                String value = line.substring(index + SEP.length()).trim();

                switch (name) {
                    case USER:
                        username = value;
                        break;
                    case PASSWORD:
                        try {
                            password = AESEncrypt.decrypt(value);
                        } catch (Exception e) {
                            Logger.logError(e);
                            Logger.logError("pass: " + value);
                        }
                        break;
                    case HOST:
                        host = value;
                        break;
                    case ACTION:
                        addAction(value, lineNr);
                        break;
                }
            }
            initialised = true;
        } catch (IOException e) {
            Logger.logError(e);
        }
    }

    private void addAction(String value, int lineNr) {
        String[] splits = value.split(PATH_SEP);
        if (splits.length != 2) {
            Logger.logError("Invalid path on line: " + lineNr + " (" + value + ")");
        }

        String paths = splits[0].trim();
        String extensions = splits[1].trim();

        boolean upload = false;
        int indicatorLen = PATH_DOWNLOAD.length();
        int indicator = paths.indexOf(PATH_DOWNLOAD);
        if (indicator < 0) {
            upload = true;
            indicatorLen = PATH_UPLOAD.length();
            indicator = paths.indexOf(PATH_UPLOAD);
            if (indicator < 0) {
                Logger.logError("No indicator in path on line: " + lineNr + " (" + paths + ")");
                return;
            }
        }

        String remote = paths.substring(0, indicator).trim();
        String local = paths.substring(indicator + indicatorLen).trim();
        // trim the individual extensions
        List<String> extensionList = Arrays.stream(extensions.split(PATH_EXT_SEP)).map(String::trim).collect(Collectors.toList());
        actionDescriptors.add(new ActionDescriptor(remote, local, upload, extensionList));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public List<ActionDescriptor> getActionDescriptors() {
        return actionDescriptors;
    }

    public boolean isInitialised() {
        return initialised;
    }
}
