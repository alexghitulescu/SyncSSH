package com.adg.sync.ssh.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adg on 11/06/2015.
 */
public class FileInfo {

    private String name, relativePath, fullName;
    private long lastModified;
    private boolean folder;

    public FileInfo(String name, long lastModified, String relativePath) {
        this(name, lastModified, relativePath, false);
    }

    public FileInfo(String name, long lastModified, String relativePath, boolean folder) {
        this.name = name;
        this.lastModified = lastModified;
        this.relativePath = relativePath.trim().replace('\\', '/');
        this.folder = folder;

        if (!relativePath.isEmpty()) {
            fullName = relativePath + "/" + name;
        } else {
            fullName = name;
        }
    }

    public String getName() {
        return name;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isFolder() {
        return folder;
    }

    public boolean isFile() {
        return !folder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) obj;
            return fullName.equals(fileInfo.fullName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    @Override
    public String toString() {
        return "FileInfo {" + fullName + ", " + lastModified + ", " + (folder ? "folder" : "file") + ")";
    }

    public static List<FileInfo> getLocalFiles(String rootPath, String path, List<String> extensions, boolean recursive) {
        // sanity check
        if (path == null) {
            path = "";
        }
        path = path.trim();

        ArrayList<FileInfo> localFiles = new ArrayList<>();
        File folder = new File(rootPath + "/" + path);
        if (!folder.exists()) {
            Logger.logError("folder: " + rootPath + " does not exist");
            return localFiles;
        }
        File[] files = folder.listFiles();

        if (files == null) {
            Logger.logError("no files in folder: " + rootPath);
            return localFiles;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (recursive) {
                    localFiles.add(new FileInfo(file.getName(), file.lastModified() / 1000, path, true));
                    String newPath = path.isEmpty() ? "" : path + "/";
                    localFiles.addAll(getLocalFiles(rootPath, newPath + file.getName(), extensions, true));
                }
            } else if (extensions.contains("*") || extensions.contains(Util.getExtension(file.getName()))) {
                localFiles.add(new FileInfo(file.getName(), file.lastModified() / 1000, path));
            }
        }

        return localFiles;
    }
}
