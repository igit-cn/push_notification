package com.yidian.push.weather.util;

import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.HttpConnectionUtils;
import com.yidian.push.weather.data.Document;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


@Log4j
public class SmartWeatherUtil {
    private static final String SUCCESS = "success";

    public static String encrypt(String url, String privateKey, String macName, String encoding) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = privateKey.getBytes(encoding);
        SecretKey secretKey = new SecretKeySpec(data, macName);
        Mac mac = Mac.getInstance(macName);
        mac.init(secretKey);
        byte[] text = url.getBytes(encoding);
        byte[] signature = mac.doFinal(text);
        return URLEncoder.encode(new Base64().encodeAsString(signature), encoding);
    }

    public static String genDocAndGetDocId(String genDocUrl, String title, String content, String mediaId, String getDocIdUrl) {
        String weMediaId = genDoc(genDocUrl, title, content, mediaId);
        String docId = getDocId(getDocIdUrl, weMediaId);
        return docId;
    }

    public static String genDoc(String url, String title, String content, String mediaId) {
        String weMediaDocId = null;
        JSONObject json = new JSONObject();
        json.put("title", title);
        json.put("content", content);
        json.put("cate", "天气");
        json.put("media_id", mediaId);
        try {
            String response = HttpConnectionUtils.doPostJSON(url, json);
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (SUCCESS.equals(jsonObject.getString("status"))
                    && jsonObject.containsKey("result")) {
                JSONObject result = jsonObject.getJSONObject("result");
                weMediaDocId = result.getString("id");
            }

        } catch (Exception e) {
            log.error("could not gen doc with exception:" + ExceptionUtils.getFullStackTrace(e));
        }
        return weMediaDocId;
    }

    public static String getDocId(String url, String weMediaDocId) {
        String docId = null;
        Map<String, Object> args = new HashMap<>();
        args.put("is_admin", "1");
        args.put("id", weMediaDocId);
        try {
            String response = HttpConnectionUtils.getGetResult(url, args);
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (SUCCESS.equals(jsonObject.getString("status"))
                    && jsonObject.containsKey("result")) {
                docId = jsonObject.getJSONObject("result").getString("news_id");
            }

        } catch (Exception e) {
            log.error("could not get docid with exception :" + ExceptionUtils.getFullStackTrace(e));
        }
        return docId;
    }

    public static String getLocalChannel(String url, String location) throws IOException {
        String res = null;
        Map<String, Object> args = new HashMap<>();
        args.put("location", location);
        String response = HttpConnectionUtils.getGetResult(url, args);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (SUCCESS.equals(jsonObject.getString("status"))
                && jsonObject.containsKey("result")) {
            res = jsonObject.getJSONObject("result").getString(location);
        }

        return res;
    }

    public static boolean pushDocument(Document document, String pushUrl, String pushKey) {
        return true;
    }


}