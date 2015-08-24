package com.adg.sync.ssh.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adg on 11/06/2015.
 *
 * Holds information about a file.
 */
public class FileInfo {

    private String name, relativePath, fullName;
    private long lastModified;
    private boolean folder;

    /**
     * Creates an object representing a file
     * @param name the name of the file
     * @param lastModified last modified time in seconds from Jan 1, 1970 in UTC.
     * @param relativePath path to the folder containing the file
     */
    public FileInfo(String name, long lastModified, String relativePath) {
        this(name, lastModified, relativePath, false);
    }

    /**
     * Creates an object representing a file of folder
     * @param name the name of the file or folder
     * @param lastModified last modified time in seconds from Jan 1, 1970 in UTC.
     * @param relativePath path to the folder containing the file or folder
     * @param folder true if it's a folder, false otherwise
     */
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

    /**
     * @return name of the file or folder represented by this object
     */
    public String getName() {
        return name;
    }

    /**
     * @return the last modified time in seconds from Jan 1, 1970 in UTC.
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * @return path of the folder containing this file or folder
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * @return path to this file or folder
     */
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

    /**
     * Removes a file and prints to stdout the output. Works on folders only if they are empty
     *
     * @return the output of {@link File#delete()} on the path represented by {@link #getFullName()}
     */
    public boolean removeFile(String localFolder) {
        if (localFolder == null) {
            localFolder = "";
        } else {
            localFolder = localFolder.trim();
        }

        String path = localFolder.isEmpty() ? fullName : localFolder + "/" + fullName;
        if (!new File(path).delete()) {
            System.out.println("Could not delete the file '" + fullName + "'");
            return false;
        } else {
            Logger.logMessage("FileInfo#removeFile", "Removed the file '" + fullName + "'");
            return true;
        }
    }

    @Override
    public String toString() {
        return "FileInfo {" + fullName + ", " + lastModified + ", " + (folder ? "folder" : "file") + ")";
    }

    /**
     * Return a list of local files that are in the path represented by rootPath joined with path that have one of
     * the extensions in the list
     * @param rootPath the path to the folder to look into
     * @param path relative to rootPath, a subfolder, can be null or empty
     * @param extensions a {@link List} of accepted extensions
     * @param recursive true if it should look in subfolders
     * @return a list of local files
     */
    public static List<FileInfo> getLocalFiles(String rootPath, String path, List<String> extensions, boolean recursive) {
        // sanity check
        if (path == null) {
            path = "";
        }
        path = path.trim();

        ArrayList<FileInfo> localFiles = new ArrayList<>();
        File folder = new File(rootPath + "/" + path);
        if (!folder.exists()) {
            Logger.logMessage("folder: " + rootPath + " does not exist");
            return localFiles;
        }
        File[] files = folder.listFiles();

        if (files == null) {
            Logger.logMessage("no files in folder: " + rootPath);
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
