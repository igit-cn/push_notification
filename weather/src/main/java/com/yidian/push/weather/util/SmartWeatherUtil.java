/*
 * 构造HTTP请求的帮助类
 */
package com.yidian.push.weather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
 
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
 
import sun.misc.BASE64Encoder;

public class SmartWeatherUtil {
	    private static final String MAC_NAME = "HmacSHA1";  
	    private static final String ENCODING = "UTF-8";  
	    private static final String appid = "sjwz3i2zw1u5419n";  
	    private static final String private_key = "yidianzixun_data";
	    private static final String url_header="http://webapi.weather.com.cn/data/?"; 
	    
	    /** 
	     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
	     * @param url 被签名的字符串
	     * @param privatekey  密钥
	     * @return
	     * @throws Exception
	     */  
	    public static byte[] HmacSHA1Encrypt(String url, String privatekey) throws Exception   
	    {         
	    	byte[] data=privatekey.getBytes(ENCODING);
	    	//根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
	        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME); 
	        //生成一个指定 Mac 算法 的 Mac 对象
	        Mac mac = Mac.getInstance(MAC_NAME); 
	        //用给定密钥初始化 Mac 对象
	        mac.init(secretKey);  
	        
	        byte[] text = url.getBytes(ENCODING);  
	        //完成 Mac 操作 
	        return mac.doFinal(text);  
	    }  
	   
	    /** 
	     * 获取URL通过privatekey加密后的码 
	     * @param url 
	     * @param privatekey 
	     * @return 
	     * @throws Exception 
	     */  
	    private static String getKey(String url, String privatekey) throws Exception {  
	        byte[] key_bytes = HmacSHA1Encrypt(url, privatekey);  
	        return URLEncoder.encode(new BASE64Encoder().encode(key_bytes),ENCODING); 
	    }  
	    /** 
	     * 组装url的地址
	     * @param areaid 地区id
	     * @param type   数据类型
	     * @param date  时间
	     * @return 
	     * @throws Exception 
	     */  
	    private static String getInstanceURL(String areaid,String type,String date) throws Exception{  
	        String keyurl=url_header+"areaid="+areaid+"&type="+type+"&date="+date+"&appid=";  
//	        System.out.println(keyurl+appid);
	        String key=getKey(keyurl+appid,private_key);  
	        String appid6 = appid.substring(0, 6);
//	        System.out.println(keyurl+appid6+"&key=" + key);
	        return keyurl+appid6+"&key=" + key;  
	    }  
	    /**
	     * 获取访问URL
	     * @param areaid  地区编号
	     * @param type   获取类型数:
	     *               天气指数：index_f(基础) 、 index_v(常规)
                         3 天常规预报 (24 小时 ):forecast_f(基础 ) 、forecast_v (常规)
	     * @return
	     */
	    public static String getActionURL(String areaid,String type){  
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");  
	        String date = dateFormat.format(new Date());  
	        try {  
	            return getInstanceURL(areaid,type,date);  
	        } catch (Exception e) {  
	        }  
	        return null;  
	    }  
	    public static String sendGet(String url) throws IOException {
	        StringBuffer buffer = new StringBuffer(); //用来拼接参数
	        StringBuffer result = new StringBuffer(); //用来接受返回值
	        URL httpUrl = null; //HTTP URL类 用这个类来创建连接
	        URLConnection connection = null; //创建的http连接
	        BufferedReader bufferedReader = null; //接受连接受的参数

	        //创建URL
	        httpUrl = new URL(url);
	        //建立连接
	        connection = httpUrl.openConnection();
//	        connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	        connection.connect();
	        //接受连接返回参数
	        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        while ((line = bufferedReader.readLine()) != null) {
	            result.append(line);
	        }
	        bufferedReader.close();
	        return result.toString();
	    }
//	    /**
//	     * 测试
//	     * @param args
//	     * @throws Exception
//	     */
//	    public static void main(String[] args) throws Exception {
////			args
////	    	System.out.println(sendGet(getActionURL(args[0], args[1])));
//		System.out.println(sendGet(getActionURL("101010100", "forecast")));
////	    	System.out.println(sendGet(getActionURL("101010100", "alarm")));
////	    	System.out.println(sendGet(getActionURL("101010100", "observe")));
////	    	System.out.println(sendGet(getActionURL("101310230", "air")));
//
//	}
}