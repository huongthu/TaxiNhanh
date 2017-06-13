package com.example.thu.utils;

/**
 * Created by thu on 6/13/2017.
 */

public class Utils {
    public static boolean isNullOrEmpty(String... strs) {
        for (String str : strs) {
            if (null == strs || str.equals("")) {
                return true;
            }
        }
        return false;
    }
}
