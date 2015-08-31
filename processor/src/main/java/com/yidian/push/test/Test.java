package com.yidian.push.test;

import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.GsonFactory;
import org.codehaus.jackson.annotate.JsonValue;

import java.io.UnsupportedEncodingException;

/**
 * Created by tianyuzhi on 15/7/28.
 */
public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String aStr = "1,2,3,,,";
        System.out.println(GsonFactory.getNonPrettyGson().toJson(aStr.split(",", 8)));
        String str = "sc123";
        System.out.print(Integer.toHexString(Integer.parseInt(str.substring(2))));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "过半网友乘扶梯时“左行右立”，上海已取消规定，“左行右立”会导致梯级左右承受重量不均，在地铁客流现状下已不提倡>>详细");
        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.toString().getBytes("UTF-8").length);

        String title =     "过半网友乘扶梯时“左行右立”，上海已取消规定，“左行右立”会导致梯级左右承受重量不均，在地铁客流现状下已不提倡>>详细";
        System.out.println(title.length());
        System.out.println();

    }
}
