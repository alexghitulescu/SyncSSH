package com.adg.sync.ssh.util;

import java.util.List;

/**
 * Created by adg on 11/06/2015.
 *
 * Utility classes
 */
public class Util {

    /**
     * Parses an integer from a string
     *
     * @param text the string to be parsed
     * @param def the default value, in case the parsing fails
     * @return the parsed integer or the default value
     */
    public static int parseInt(String text, int def) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Gets and returns the extension of a file or the empty string if an extension could not be found
     *
     * @param fileName the file name
     * @return the extension or the empty string
     */
    public static String getExtension(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos != -1) {
            return fileName.substring(pos + 1);
        }
        return "";
    }

    /**
     * Looks for an object in a {@link List}. Two objects can be equal but have different fields.
     * For example a {@link FileInfo} object is equal to another if the paths are equal, but two
     * equal {@link FileInfo} objects can have different {@link FileInfo#getLastModified()} times.
     *
     * @param list the list to be searched
     * @param object the object to be found
     * @param <T> a class that implements equals correctly
     * @return the object
     */
    public static <T> T find(List<T> list, T object) {
        for (T element : list) {
            if (element.equals(object)) {
                return element;
            }
        }
        return null;
    }
}
