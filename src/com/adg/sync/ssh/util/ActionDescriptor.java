package com.adg.sync.ssh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adg on 11/06/2015.
 */
public class ActionDescriptor {

    private String remoteFolder, localFolder;

    private boolean upload;

    private List<String> extensions;

    public ActionDescriptor(String remoteFolder, String localFolder, boolean upload, String[] extensions) {
        this(remoteFolder, localFolder, upload, new ArrayList<>(Arrays.asList(extensions)));
    }

    public ActionDescriptor(String remoteFolder, String localFolder, boolean upload, List<String> extensions) {
        this.remoteFolder = remoteFolder;
        this.localFolder = localFolder;
        this.upload = upload;
        this.extensions = extensions;
        System.out.println(this);
    }

    public String getRemoteFolder() {
        return remoteFolder;
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public boolean isUpload() {
        return upload;
    }

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
