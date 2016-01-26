package com.yidian.push.weather.util;

import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.utils.HttpConnectionUtils;
import com.yidian.push.weather.data.Document;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


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
        json.put("cate", "社会");
        json.put("import_url", "http://openweather.weather.com.cn/Home/Help/Product.html"); // TODO
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



    public static boolean pushDocument(Document document, String pushUrl, String pushKey, String pushUserIds) {
        //		String url="http://lc1.haproxy.yidian.com:8703/push/add_task.php?host=push.yidian-inc.com&userids=auto_break&type[]=weather&docid="+docid+"&channel="+"u_faked"+"&key="+key;
        boolean succeeded = false;
        List<String> channelList = new ArrayList<>();
        for (String channel : document.getFromIdPushed().keySet()) {
            boolean channelPushed = document.getFromIdPushed().getOrDefault(channel, true);
            if (!channelPushed) {
                channelList.add(channel);
            }
        }
        if (channelList.size() == 0) {
            log.info("no channel to push for docid [" + document.getDocId()
                    + "], alarmId [" + document.getAlarmId() + "]");
            succeeded = true;
        }
        else {
            String channels = StringUtils.join(channelList, ",");
            succeeded = pushDocument(document, pushUrl, pushKey, pushUserIds, channels);
        }
        return succeeded;
    }

    public static boolean pushDocument(Document document, String pushUrl, String pushKey, String pushUserIds, String channels) {
        boolean succeeded = false;
        if (StringUtils.isEmpty(channels)) {
            succeeded = true;
        }
        else {
            Map<String, Object> params = new HashMap<>();
            params.put("key", pushKey);
            params.put("userids", pushUserIds);
            params.put("docid", document.getDocId());
            params.put("type", Arrays.asList("weather"));
            params.put("channel", channels);

            try {
                String response = HttpConnectionUtils.getGetResult(pushUrl, params);
                JSONObject jsonObject = JSONObject.parseObject(response);
                if (jsonObject.containsKey("status") && SUCCESS.equals(jsonObject.getString("status"))) {
                    log.info(GsonFactory.getNonPrettyGson().toJson(document) + " push succeeded");
                    succeeded = true;
                }
                else {
                    succeeded = false;
                    log.error(GsonFactory.getNonPrettyGson().toJson(document) + " push failed with reason:"
                            + jsonObject.getString("reason"));
                }
            } catch (IOException e) {
                log.error(GsonFactory.getNonPrettyGson().toJson(document) + " push failed with reason:"
                        + ExceptionUtils.getFullStackTrace(e));
                succeeded = false;
            }
        }
        return succeeded;
    }


}