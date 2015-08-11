package com.adg.sync.ssh;

import com.adg.sync.ssh.util.*;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by adg on 11/06/2015.
 *
 * An object that can be used to maintain a connection to the server
 */
public class Connector {

    private JSch jsch;
    private Session session;
    private Initializer initializer;

    /**
     * Creates a new connector and uses the initializer to get the data required for connection
     * @param initializer the initializer object, has to be initialized
     */
    public Connector(Initializer initializer) {
        this.initializer = initializer;
        this.jsch = new JSch();
    }

    /**
     * Starts a connection
     * @return a {@link com.jcraft.jsch.Session} or null if the connection was unsuccessful
     */
    public Session connect() {
        disconnect();

        try {
            Session session = jsch.getSession(initializer.getUsername(), initializer.getHost(), 22);
            session.setPassword(initializer.getPassword());
            session.setUserInfo(new AutomatedUserInfo(initializer));
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect(10000);

            this.session = session;
        } catch (Exception e) {
            Logger.logError(e);
        }
        return session;
    }

    /**
     * Disconnects the session
     */
    public void disconnect() {
        if (session != null) {
            try {
                session.disconnect();
            } catch (Exception e) {
            }
            session = null;
        }
    }

    /**
     * Checks if a session is alive
     * @return
     */
    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public Session getSession() {
        return session;
    }

    /**
     * Returns a {@link List} of {@link FileInfo} with all files in the folder given by rootFolder
     * and folder that have one of the specified extensions.
     *
     * @param rootFolder the root folder to look into
     * @param folder a subfolder, can be empty or null
     * @param extensions a {@code List} of extensions to be accepted
     * @param recursive true if the algorithm should look into subfolders
     * @return a {@link List} of {@link FileInfo}
     */
    public List<FileInfo> getRemoteFiles(String rootFolder, String folder, List<String> extensions, boolean recursive) {
        // sanity check
        if (folder == null) {
            folder = "";
        }
        folder = folder.trim();

        if (!isConnected()) {
            connect();
            if (!isConnected()) {
                Logger.logError(new Exception("Could not connect to remote host"));
                return new ArrayList<>();
            }
        }

        ArrayList<FileInfo> files = new ArrayList<>();
        ArrayList<FileInfo> folders = new ArrayList<>();

        try {
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(10000);

            Vector fileList;
            try {
                fileList = channelSftp.ls(rootFolder + "/" + folder);
            } catch (Exception e) {
                Exception ex = new Exception("(" + rootFolder + "/" + folder + ")", e);
                Logger.logError(ex);
                ex.printStackTrace();
                return files;
            }

            for (Object file : fileList) {
                if (file instanceof ChannelSftp.LsEntry) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) file;
                    // ignore files that start with a dot
                    if (entry.getFilename().startsWith(".")) {
                        continue;
                    }
                    if (entry.getAttrs().isDir()) {
                        if (recursive) {
                            FileInfo fileInfo = new FileInfo(entry.getFilename(), entry.getAttrs().getMTime(), folder, true);
                            files.add(fileInfo);
                            folders.add(fileInfo);
                        }
                    } else if (extensions.contains("*") || extensions.contains(Util.getExtension(entry.getFilename()))) {
                        files.add(new FileInfo(entry.getFilename(), entry.getAttrs().getMTime(), folder));
                    }
                } else {
                    Logger.logError("Invalid file type: " + file.getClass().getSimpleName() + " ; obj: " + file.toString());
                }
            }
            channelSftp.disconnect();
        } catch (Exception e) {
            Logger.logError(e);
            e.printStackTrace();
        }
        // get files from the folders
        String newFolder = folder.isEmpty() ? "" : folder + "/";
        folders.stream().forEachOrdered(fileInfo -> files.addAll(getRemoteFiles(rootFolder, newFolder + fileInfo.getName(), extensions, true)));
        return files;
    }
}
