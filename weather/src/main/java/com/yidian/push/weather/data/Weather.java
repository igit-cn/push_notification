package com.yidian.push.weather.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.WeatherConfig;
import com.yidian.push.utils.HttpConnectionUtils;
import com.yidian.push.weather.exception.UrlGenerationException;
import com.yidian.push.weather.util.AreaUtil;
import com.yidian.push.weather.util.SmartWeatherUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 16/1/18.
 */
@Log4j
@Getter
public class Weather {
    private static final String ITEM_SEPARATOR = ";";
    private static final String KEY_VALUE_SEPARATOR = ",";


    private Map<String, String> areaToIdMapping = new HashMap<>();
    private Map<String, String> idToAreaMapping = new HashMap<>();
    private Map<String, String> windDirectionToIdMapping = new HashMap<>();
    private Map<String, String> idToWindDirectionMapping = new HashMap<>();
    private Map<String, String> windPowerToIdMapping = new HashMap<>();
    private Map<String, String> idToWindPowerMapping = new HashMap<>();
    private Map<String, String> weatherPhenomenaToIdMapping = new HashMap<>();
    private Map<String, String> idToWeatherPhenomenaMapping = new HashMap<>();
    private Map<String, String> alarmCategoryToIdMapping = new HashMap<>();
    private Map<String, String> idToAlarmCategoryMapping = new HashMap<>();
    private Map<String, String> alarmLevelToIdMapping = new HashMap<>();
    private Map<String, String> idToAlarmLevelMapping = new HashMap<>();
    // for guangdong
    private Map<String, String> guangdongAlarmCategoryToIdMapping = new HashMap<>();
    private Map<String, String> idToGuangdongAlarmCategoryMapping = new HashMap<>();
    private Map<String, String> guangdongAlarmLevelToIdMapping = new HashMap<>();
    private Map<String, String> idToGuangdongAlarmLevlMapping = new HashMap<>();
    private Map<String, String> idToGuangdongAreaMapping = new HashMap<>();
    private Map<String, String> guangdongAreaToIdMapping = new HashMap<>();


    private String macAlgorithmName = "HmacSHA1";
    private String encoding = "UTF-8";
    private String appId = "sjwz3i2zw1u5419n";
    private String privateKey = "yidianzixun_data";
    private String url = "http://webapi.weather.com.cn/data/";

    private void updateMapping(Map<String, String> keyValueMapping, Map<String, String> valueKeyMapping, String keyValues) {
        if (StringUtils.isEmpty(keyValues)) {return;}
        for (String keyValue : keyValues.split(ITEM_SEPARATOR)) {
            String[] arr = keyValue.split(KEY_VALUE_SEPARATOR);
            if (arr.length == 2) {
                String key = arr[0];
                String value = arr[1];
                if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                    keyValueMapping.put(key, value);
                    valueKeyMapping.put(value, key);
                }
            }
        }

    }
    public Weather(WeatherConfig weatherConfig) {
        updateMapping(idToAreaMapping, areaToIdMapping, weatherConfig.getIdToAreaStr());
        updateMapping(idToWindDirectionMapping, windDirectionToIdMapping, weatherConfig.getIdToWindDirectionStr());
        updateMapping(idToWindPowerMapping, windPowerToIdMapping, weatherConfig.getIdToWindPowerStr());
        updateMapping(idToWeatherPhenomenaMapping, weatherPhenomenaToIdMapping, weatherConfig.getIdToWeatherPhenomenaStr());
        updateMapping(idToAlarmCategoryMapping, alarmCategoryToIdMapping, weatherConfig.getIdToAlarmCategoryStr());
        updateMapping(idToAlarmLevelMapping, alarmLevelToIdMapping, weatherConfig.getIdToAlarmLevelStr());
        // for guangdong alarm
        updateMapping(idToGuangdongAlarmCategoryMapping, guangdongAlarmCategoryToIdMapping, weatherConfig.getIdToGuangdongAlarmCategoryStr());
        updateMapping(idToGuangdongAlarmLevlMapping, guangdongAlarmLevelToIdMapping, weatherConfig.getIdToGuangdongAlarmLevelStr());
        updateMapping(idToGuangdongAreaMapping, guangdongAreaToIdMapping, weatherConfig.getIdToGuangdongAreaStr());


        this.macAlgorithmName = weatherConfig.getMacAlgorithmName();
        this.encoding = weatherConfig.getEncoding();
        this.appId = weatherConfig.getAppId();
        this.privateKey = weatherConfig.getPrivateKey();
        this.url = weatherConfig.getUrl();
    }

    public String getArea(String areaId) {
        return idToAreaMapping.get(areaId);
    }



    public String genUrl(String areaId, WeatherType weatherType, String date) throws UrlGenerationException {
        String baseUrl = url + "?areaid=" + areaId + "&type=" + weatherType.getName() + "&date=" + date + "&appid=";
        String encryptedKey;
        try {
            encryptedKey = SmartWeatherUtil.encrypt(baseUrl + appId, privateKey, macAlgorithmName, encoding);
        } catch (Exception e) {
            throw new UrlGenerationException("url generation error : " + e.getMessage());
        }
        String subAppId = appId.substring(0, 6);
        return baseUrl + subAppId + "&key=" + encryptedKey;
    }

    public List<Alarm> getAreaIdAlarms(String areaId) throws IOException, UrlGenerationException {
        if (StringUtils.isEmpty(areaId) || !idToAreaMapping.containsKey(areaId)) {
            return new ArrayList<>(0);
        }
        String content = getWeatherData(areaId, WeatherType.ALARM);
        JSONObject jsonObject = JSON.parseObject(content);
        List<Alarm> alarmList = new ArrayList<>();
        if (jsonObject.containsKey("w")) {
            JSONArray jsonArray = jsonObject.getJSONArray("w");
            for(Object obj : jsonArray) {
                JSONObject item = (JSONObject)obj;
                Alarm alarm = new Alarm();
                alarm.setProvince(item.getString("w1"));
                alarm.setCity(item.getString("w2"));
                alarm.setCounty(item.getString("w3"));
                alarm.setCategoryId(item.getString("w4"));
                alarm.setCategoryName(item.getString("w5"));
                alarm.setLevelId(item.getString("w6"));
                alarm.setLevelName(item.getString("w7"));
                alarm.setPublishTime(item.getString("w8"));
                alarm.setContent(item.getString("w9"));
                alarm.setId(item.getString("w10"));
                alarmList.add(alarm);
            }
        }
        return alarmList;
    }
    public boolean isInGuangdong(String areaId) {
        return idToGuangdongAreaMapping.containsKey(areaId);
    }

    public List<Alarm> getAreaAlarms(String area) throws IOException, UrlGenerationException {
        area = AreaUtil.normalize(area);
        if (areaToIdMapping.containsKey(area)) {
            return getAreaIdAlarms(areaToIdMapping.get(area));
        }
        return new ArrayList<>(0);
    }

    public String getWeatherData(String areaId, WeatherType weatherType) throws IOException, UrlGenerationException {
        String dateTime = DateTime.now().toString("yyyyMMddHHmm");
        String queryUrl = genUrl(areaId, weatherType, dateTime);
        if (StringUtils.isEmpty(queryUrl)) {
            return null;
        }
        log.info("get weather data from url:" + queryUrl);
        String response = HttpConnectionUtils.getGetResult(queryUrl, null);
        String encoding = getEncoding(response);
        response = new String(response.getBytes(encoding), "UTF-8");
        return response;
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }


}
