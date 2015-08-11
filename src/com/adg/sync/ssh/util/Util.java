package com.adg.sync.ssh.util;

import java.util.List;

/**
 * Created by adg on 11/06/2015.
 */
public class Util {

    public static int parseInt(String text, int def) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static String getExtension(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos != -1) {
            return fileName.substring(pos + 1);
        }
        return "";
    }

    public static <T> T find(List<T> list, T object) {
        for (T element : list) {
            if (element.equals(object)) {
                return element;
            }
        }
        return null;
    }
}
