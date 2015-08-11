package com.adg.sync.ssh.util;

import com.jcraft.jsch.UserInfo;

/**
 * Created by adg on 11/06/2015.
 */
public class AutomatedUserInfo implements UserInfo {

    private final Initializer initializer;

    public AutomatedUserInfo(Initializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public String getPassphrase() {
        return "";
    }

    @Override
    public String getPassword() {
        return initializer.getPassword();
    }

    @Override
    public boolean promptPassword(String message) {
        Logger.logMessage("promptPassword", message);
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        Logger.logMessage("promptPassphrase", message);
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        Logger.logMessage("promptYesNo", message);
        return true;
    }

    @Override
    public void showMessage(String message) {
        Logger.logMessage("showMessage", message);
    }
}
