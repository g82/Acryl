package com.gamepari.acryl.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gamepari on 12/21/14.
 */
public class ParserUtil {

    public static final InputStream downloadURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        conn.connect();
        InputStream stream = conn.getInputStream();

        return stream;
    }

    public static final String makeStringFromStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();

        String inputStr;

        while ((inputStr = bufferedReader.readLine()) != null) {
            stringBuilder.append(inputStr);
        }

        inputStream.close();

        return stringBuilder.toString();
    }

}
