package com.yidian.push.utils;

import com.alibaba.fastjson.JSONObject;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yidianadmin on 15-4-29.
 */
@Log4j
public class HttpConnectionUtils {
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

    public static synchronized void init(int maxTotal, int defaultMaxPerRoute) throws IOException {
        release();
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        // 每条通道的并发连接数设置（连接池）
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);

    }
    public static synchronized void release() {
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

    public static String getPostResult(String url, Map<String, Object> params, Map<String, String> headers, RequestConfig config) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        try {

            CloseableHttpClient client =  HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).build();
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            if (null != params) {
                for (String key : params.keySet()) {
                    Object object = params.get(key);
                    if (object instanceof String) {
                        nameValuePairList.add(new BasicNameValuePair(key, (String)object));
                    }
                    else if (object instanceof List) {
                        for (Object item : (List)object) {
                            nameValuePairList.add(new BasicNameValuePair(key, (String)item));
                        }
                    }
                    else {
                        nameValuePairList.add(new BasicNameValuePair(key, String.valueOf(object)));
                    }
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8.toString()));
            }
            if (null != headers && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpPost.addHeader(key, headers.get(key));
                }
            }
            String str = client.execute(httpPost, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws IOException {
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

    public static String getPostResult(String url, Map<String, Object> params, RequestConfig config) throws IOException {
        return getPostResult(url, params, null, config);
    }

    public static String getPostResult(String url, Map<String, Object> params) throws IOException {
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        return getPostResult(url, params, config);
    }

    public static String getPostContent(String url, Map<String, Object> params) throws IOException {
        return getPostResult(url, params);
    }


    // add get methods, this is not a good way to do this...

    public static String getGetResult(String url, Map<String, Object> params, Map<String, String> headers, RequestConfig config, boolean automaticRetry) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        String result = null;
        try {
            HttpClientBuilder httpClientBuilder =  HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config);
            if (!automaticRetry) {
                httpClientBuilder.disableAutomaticRetries();
            }
            CloseableHttpClient client =  httpClientBuilder.build();
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            if (null != params ) {
                for (String key : params.keySet()) {
                    Object object = params.get(key);
                    if (object instanceof String) {
                        nameValuePairList.add(new BasicNameValuePair(key, (String)object));
                    }
                    else if (object instanceof List) {
                        for (Object item : (List)object) {
                            nameValuePairList.add(new BasicNameValuePair(key, (String)item));
                        }
                    }
                    else {
                        nameValuePairList.add(new BasicNameValuePair(key, String.valueOf(object)));
                    }
                }
                String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8.toString()), StandardCharsets.UTF_8.toString());
                httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + paramStr));
            }
            if (null != headers && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpGet.addHeader(key, headers.get(key));
                }
            }
            result = client.execute(httpGet, new ResponseHandler<String>() {
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
        } catch (URISyntaxException e) {
            log.error("bad request params.");
        } finally {
            httpGet.releaseConnection();
        }
        return result;
    }

    public static String getGetResult(String url, Map<String, Object> params, RequestConfig config) throws IOException {
        return getGetResult(url, params, null, config, true);
    }

    public static String getGetResult(String url, Map<String, Object> params, RequestConfig config, boolean needRetry) throws IOException {
        return getGetResult(url, params, null, config, needRetry);
    }

    public static String getGetResult(String url, Map<String, Object> params) throws IOException {
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        return getGetResult(url, params, config, true);
    }

    public static String getGetResult(String url, Map<String, Object> params, boolean needRetry) throws IOException {
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        return getGetResult(url, params, config, needRetry);
    }

    public static String doPostJSON(String url,JSONObject json) throws IOException {
        HttpPost post = new HttpPost(url);
        String response = null;
        try {
            HttpClientBuilder httpClientBuilder =  HttpClients.custom().setConnectionManager(cm);
            CloseableHttpClient client =  httpClientBuilder.build();

            String string = json.toString();
            StringEntity s = new StringEntity(string, "UTF-8");
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);

            response = client.execute(post, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws IOException {
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
            return response;
        } finally {
            post.releaseConnection();
        }

    }

}