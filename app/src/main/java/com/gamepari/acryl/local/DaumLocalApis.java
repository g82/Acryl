package com.gamepari.acryl.local;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by gamepari on 12/21/14.
 */
public class DaumLocalApis {

    private static final String BASE_API_URL = "http://apis.daum.net/local/geo/addr2coord";

    public static String makeQueryURL(String apiKey, String q) {

        StringBuilder stringBuilder = new StringBuilder(BASE_API_URL);

        stringBuilder.append("?apikey=" + apiKey);
        try {
            stringBuilder.append("&q=" + URLEncoder.encode(q, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        stringBuilder.append("&output=json");

        return stringBuilder.toString();
    }

}
