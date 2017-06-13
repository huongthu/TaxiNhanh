package com.example.thu.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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

    //https://stackoverflow.com/questions/2075836/read-contents-of-a-url-in-android
    public static String getContentFromUrl (String urlStr) {
        String ret = "";
        URL url = null;
        BufferedReader in = null;
        String inputLine;
        try {
            url = new URL(urlStr);
            in = new BufferedReader(
                    new InputStreamReader(
                            url.openStream()));
            while ((inputLine = in.readLine()) != null) {
                ret += inputLine;
            }
            in.close();
        } catch ( IOException ioe) {
            ret = "";
        }
        return ret;
    }
}
