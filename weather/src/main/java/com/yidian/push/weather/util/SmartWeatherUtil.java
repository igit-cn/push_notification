/*
 * 构造HTTP请求的帮助类
 */
package com.yidian.push.weather.util;

import com.alibaba.fastjson.JSONObject;
import com.yidian.push.weather.data.Document;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class SmartWeatherUtil {

    public static void getDataFromMongo() {

    }

    public static void genDodId() {

    }

    public static String getDocIdFrom() {
        return null;
    }

    public static boolean pushDocument(Document document, String pushUrl, String pushKey) {
        return true;
    }



}