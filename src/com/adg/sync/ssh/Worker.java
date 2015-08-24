package com.adg.sync.ssh;

import com.adg.sync.ssh.util.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adg on 11/06/2015.
 *
 * The main part of this project, It uses the other objects to connect, and perform the specified actions.
 */
public class Worker {

    private final boolean upload;
    private Connector connector;
    private Initializer initializer;

    /**
     * Constructs a Worker object, initializes the application and start the connection.
     * @param path path to the folder containing SyncSSH.ini
     * @param upload true if the desired action is upload, false if it is download
     */
    public Worker(String path, boolean upload) {
        this.upload = upload;

        initializer = new Initializer();
        initializer.init(path);

        connector = new Connector(initializer);
        connector.connect();
    }

    /**
     * Disconnects the connection.
     */
    public void close() {
        connector.disconnect();
    }

    /**
     * performs the upload / download actions
     */
    public void sync() {
        for (ActionDescriptor actionDescriptor : initializer.getActionDescriptors()) {
            if (actionDescriptor.isUpload() != upload) {
                continue;
            }

            if (!upload) {
                download(actionDescriptor);
            } else {
                upload(actionDescriptor);
            }
        }
    }

    /**
     * Requires a download {@link ActionDescriptor}. Downloads the files specified by the action.
     * @param actionDescriptor action to be performed
     */
    public void download(ActionDescriptor actionDescriptor) {
        List<FileInfo> remoteFiles = connector.getRemoteFiles(actionDescriptor.getRemoteFolder(), "", actionDescriptor.getExtensions(), true);
        List<FileInfo> localFiles = FileInfo.getLocalFiles(actionDescriptor.getLocalFolder(), "", actionDescriptor.getExtensions(), true);
        AtomicInteger fileNr = new AtomicInteger(0);

        // download new files
        remoteFiles.stream().filter(FileInfo::isFile).forEachOrdered(remoteFile -> {

            FileInfo localFile = Util.find(localFiles, remoteFile);

            if (localFile == null || localFile.getLastModified() < remoteFile.getLastModified()) {
                try {
                    File folder = new File(actionDescriptor.getLocalFolder() + "/" + remoteFile.getRelativePath());
                    if (!folder.exists()) {
                        if(!folder.mkdirs()) {
                            Logger.logMessage("WARNING", "could not create folder " + folder.getAbsolutePath());
                        }
                    }
                    String file = actionDescriptor.getRemoteFolder() + "/" + remoteFile.getFullName();
                    if (FileDownloader.downloadFile(connector.getSession(), file, remoteFile.getRelativePath(), actionDescriptor.getLocalFolder())) {
                        fileNr.incrementAndGet();
                        System.out.println("downloaded " + remoteFile.getFullName());
                    } else {
                        System.out.println("failed to download " + remoteFile.getFullName());
                    }
                } catch (Exception e) {
                    Logger.logError(e);
                }
            }
        });

        localFiles.stream().filter(FileInfo::isFile)
                .filter(localFile -> !remoteFiles.contains(localFile))
                .forEach(localFile -> localFile.removeFile(actionDescriptor.getLocalFolder()));

        Logger.logMessage("worker", "downloaded " + fileNr.get() + " files");
    }

    /**
     * Requires an upload {@link ActionDescriptor}. Uploads the files specified by the action.
     * @param actionDescriptor action to be performed
     */
    public void upload(ActionDescriptor actionDescriptor) {
        List<FileInfo> remoteFiles = connector.getRemoteFiles(actionDescriptor.getRemoteFolder(), "", actionDescriptor.getExtensions(), true);
        List<FileInfo> localFiles = FileInfo.getLocalFiles(actionDescriptor.getLocalFolder(), "", actionDescriptor.getExtensions(), true);
        AtomicInteger fileNr = new AtomicInteger(0);

        localFiles.stream().filter(FileInfo::isFile).forEachOrdered(localFile -> {

            FileInfo remoteFile = Util.find(remoteFiles, localFile);

            if (remoteFile == null || remoteFile.getLastModified() < localFile.getLastModified()) {
                try {

                    String file = actionDescriptor.getLocalFolder() + "/" + localFile.getFullName();
                    String remoteFolder = actionDescriptor.getRemoteFolder() + (localFile.getRelativePath().isEmpty() ? "" : "/" + localFile.getRelativePath());

                    if (FileUploader.uploadFile(connector.getSession(), file, localFile.getName(), remoteFolder)) {
                        fileNr.incrementAndGet();
                        System.out.println("uploaded " + localFile.getFullName());
                    } else {
                        System.out.println("failed to upload " + localFile.getFullName());
                    }
                } catch (Exception e) {
                    Logger.logError(e);
                }
            }
        });

        remoteFiles.stream().filter(FileInfo::isFile)
                .filter(remoteFile -> !localFiles.contains(remoteFile))
                .forEach(remoteFile -> connector.removeRemoteFile(actionDescriptor.getRemoteFolder() + "/" + remoteFile.getFullName()));

        Logger.logMessage("worker", "uploaded " + fileNr.get() + " files");
    }
}
