package com.adg.sync.ssh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adg on 11/06/2015.
 *
 * Describes an download/upload action
 */
public class ActionDescriptor {

    private String remoteFolder, localFolder;

    private boolean upload;

    private List<String> extensions;

    /**
     *
     * @param remoteFolder path to the remote folder
     * @param localFolder path to the local folder
     * @param upload true if it's an upload action false otherwise
     * @param extensions an array of extensions
     */
    public ActionDescriptor(String remoteFolder, String localFolder, boolean upload, String[] extensions) {
        this(remoteFolder, localFolder, upload, new ArrayList<>(Arrays.asList(extensions)));
    }

    /**
     *
     * @param remoteFolder path to the remote folder
     * @param localFolder path to the local folder
     * @param upload true if it's an upload action false otherwise
     * @param extensions a {@link List} of extensions
     */
    public ActionDescriptor(String remoteFolder, String localFolder, boolean upload, List<String> extensions) {
        this.remoteFolder = remoteFolder;
        this.localFolder = localFolder;
        this.upload = upload;
        this.extensions = extensions;
        System.out.println(this);
    }

    /**
     *
     * @return the remote path
     */
    public String getRemoteFolder() {
        return remoteFolder;
    }

    /**
     *
     * @return the local path
     */
    public String getLocalFolder() {
        return localFolder;
    }

    /**
     *
     * @return true if it's an upload action, false if it's a download action
     */
    public boolean isUpload() {
        return upload;
    }

    /**
     *
     * @return a {@code List} of the extensions that should be considered
     */
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ActionDescriptor {");
        sb.append(remoteFolder).append(" ");
        if (!upload) {
            sb.append(Initializer.PATH_DOWNLOAD);
        } else {
            sb.append(Initializer.PATH_UPLOAD);
        }
        sb.append(" ").append(localFolder).append(";");
        sb.append(extensions).append("}");
        return sb.toString();
    }
}
