package com.yidian.push.utils;

import lombok.extern.log4j.Log4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yidianadmin on 15-4-29.
 */
@Log4j
public class HttpClientUtils {
    private static ThreadSafeClientConnManager cm  = null;
    static {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        cm = new ThreadSafeClientConnManager(schemeRegistry);
        int maxTotal = 1000;
        cm.setMaxTotal(maxTotal);

        // 每条通道的并发连接数设置（连接池）
        int defaultMaxConnection = 200;
        cm.setDefaultMaxPerRoute(defaultMaxConnection);

    }
    public static HttpClient getHttpClient() {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000); // 3000ms for connection
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000); // 3000ms for read
        return new DefaultHttpClient(cm, params);
    }

    public static void release() {
        if (cm != null) {
            cm.shutdown();
        }
    }

    public static HttpResponse doPost(String url, Map<String, String> params) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        try {
            HttpClient client = getHttpClient();
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (String key : params.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key, params.get(key)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8.toString()));
            HttpResponse response = client.execute(httpPost);
            return response;
        } finally {
            httpPost.releaseConnection();
        }
    }

    public static String getResponseContent(HttpResponse httpResponse) throws IOException {
        return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8.toString());
    }

    public static String getPostContent(String url, Map<String, String> params) throws IOException {
        HttpResponse httpResponse = doPost(url, params);
        return getResponseContent(httpResponse);
    }
}