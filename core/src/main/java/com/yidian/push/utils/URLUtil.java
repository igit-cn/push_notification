package com.yidian.push.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by yidianadmin on 14-10-14.
 */
public class URLUtil {
    public static String getContent(String url) throws IOException {
        return getContent(new URL(url));
    }


    public static String getContent(URL url) throws IOException {
        return getContent(url, 1000, 1000);
    }

    public static String getContent(URL url, int connectTimeout, int readTimeout) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(connectTimeout);
        urlConnection.setReadTimeout(readTimeout);

        BufferedReader br = null;
        StringBuilder buffer = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String str;
            while ((str = br.readLine()) != null) {
                buffer.append(str).append('\n');
            }
        } finally {
            if (null != br) {
                br.close();
            }
        }
        return buffer.toString();
    }

    public static String getHTTPURLContent(URL url, int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(connectTimeout);
        httpURLConnection.setReadTimeout(readTimeout);
        BufferedReader br = null;
        StringBuilder buffer = new StringBuilder();
        boolean errorHappened = false;

        try {
            br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
            String str;
            while ((str = br.readLine()) != null) {
                buffer.append(str).append('\n');
            }
        } catch (IOException e) {
            errorHappened = true;
            try {
                InputStream is = httpURLConnection.getErrorStream();
                int ret = 0;
                byte[] buf = new byte[1024];
                while ((ret = is.read(buf)) > 0) {
                    //continue; //read all the data
                }
            } catch  (IOException e1) {
                errorHappened = true;
            }
            e.printStackTrace();
        } finally {
            if (null != br) {
                br.close();
            }
        }
        return errorHappened ? null : buffer.toString();

    }
}
