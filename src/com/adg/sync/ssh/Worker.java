package com.adg.sync.ssh;

import com.adg.sync.ssh.util.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adg on 11/06/2015.
 */
public class Worker {

    private final boolean upload;
    private Connector connector;
    private Initializer initializer;

    public Worker(String path, boolean upload) {
        this.upload = upload;

        initializer = new Initializer();
        initializer.init(path);

        connector = new Connector(initializer);
        connector.connect();
    }

    public void close() {
        connector.disconnect();
    }

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

    public void download(ActionDescriptor actionDescriptor) {
        List<FileInfo> remoteFiles = connector.getRemoteFiles(actionDescriptor.getRemoteFolder(), "", actionDescriptor.getExtensions(), true);
        List<FileInfo> localFiles = FileInfo.getLocalFiles(actionDescriptor.getLocalFolder(), "", actionDescriptor.getExtensions(), true);
        AtomicInteger fileNr = new AtomicInteger(0);

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

        Logger.logMessage("worker", "downloaded " + fileNr.get() + " files");
    }

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

        Logger.logMessage("worker", "uploaded " + fileNr.get() + " files");
    }
}
