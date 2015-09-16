package com.yidian.push.recommend_gen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.Getter;

import java.io.IOException;

/**
 * Created by tianyuzhi on 15/9/14.
 */
@Getter
public class QPSGetter {
    private String url = "http://dataplatform.yidian.com:4242/api/query?start=3m-ago&m=sum:prediction.default.qps.m1";
    private volatile int qps = 0;

    public QPSGetter(String url) {
        this.url = url;
    }

    public void refresh() throws IOException {
        String jsonStr = HttpConnectionUtils.getGetResult(url, null);
        JSONArray jsonArray = JSONArray.parseObject(jsonStr, JSONArray.class);

        if (null != jsonArray
                && jsonArray.size() >= 1) {
            JSONObject jsonObject = (JSONObject)jsonArray.get(0);
            if (null != jsonObject
                    && jsonObject.containsKey("dps")) {
                JSONObject dps = (JSONObject)jsonObject.get("dps");
                boolean found = false;
                int newQps = Integer.MIN_VALUE;
                for (String key : dps.keySet()) {
                    int cur = (int)dps.getDoubleValue(key);
                    found = true;
                    if (cur > newQps) {
                        newQps = cur;
                    }
                }
                if (found && newQps > 0) {
                    qps = newQps;
                }
            }
        }
    }
}
