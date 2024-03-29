package com.yidian.push.utils;

import lombok.extern.log4j.Log4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yidianadmin on 15-4-29.
 */
@Log4j
public class HttpClientUtils {
    private static PoolingHttpClientConnectionManager cm  = null;
    static {
        cm = new PoolingHttpClientConnectionManager();
        int maxTotal = 1000;
        cm.setMaxTotal(maxTotal);
        // 每条通道的并发连接数设置（连接池）
        int defaultMaxConnection = 200;
        cm.setDefaultMaxPerRoute(defaultMaxConnection);

    }
    public static HttpClient getHttpClient() {
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    public static void release() {
        if (cm != null) {
            cm.shutdown();
        }
    }

    public static HttpResponse doPost(String url, Map<String, String> params) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        try {
            int timeout = 5;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
            HttpClient client =  HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).build();
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

    public static String getPostResult(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        try {
            int timeout = 5;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
            CloseableHttpClient client =  HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).build();
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (String key : params.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key, params.get(key)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8.toString()));
            if (null != headers && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpPost.addHeader(key, headers.get(key));
                }
            }
            String str = client.execute(httpPost, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    StatusLine statusLine = httpResponse.getStatusLine();
                    HttpEntity entity = httpResponse.getEntity();
                    if (statusLine.getStatusCode() >= 300) {
                        throw new HttpResponseException(
                                statusLine.getStatusCode(),
                                statusLine.getReasonPhrase());
                    }
                    if (entity == null) {
                        throw new ClientProtocolException("Response contains no content");
                    }
                    ContentType contentType = ContentType.getOrDefault(entity);
                    Charset charset = contentType.getCharset();
                    return EntityUtils.toString(entity, charset);
                }
            });
            return str;
        } finally {
            httpPost.releaseConnection();
        }
    }

    public static String getPostResult(String url, Map<String, String> params) throws IOException {
        return getPostResult(url, params, null);
    }

    public static String getPostContent(String url, Map<String, String> params) throws IOException {
        return getPostResult(url, params);
//        HttpResponse httpResponse = doPost(url, params);
//        return getResponseContent(httpResponse);
    }

//    public static HttpResponse doGet(String url, Map<String, String> params) throws IOException {
//        HttpGet httpGet = new HttpGet(url);
//        try {
//            HttpClient client = getHttpClient();
//            HttpParams httpParams = new BasicHttpParams();
//            for (String key : params.keySet()) {
//                httpParams.setParameter(key, params.get(key));
//            }
//            httpGet.setParams(httpParams);
//            HttpResponse response = client.execute(httpGet);
//            return response;
//        } finally {
//            httpGet.releaseConnection();
//        }
//    }
}