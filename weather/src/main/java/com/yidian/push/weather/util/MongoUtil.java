package com.yidian.push.weather.util;

import com.mongodb.MongoClient;
import com.yidian.push.config.Config;
import com.yidian.push.config.InstantPushConfig;

import java.io.IOException;

/**
 * Created by tianyuzhi on 16/1/21.
 */
public class MongoUtil {
    public static volatile boolean initialized = false;
    public static MongoClient mongoClient = null;
    private static InstantPushConfig config = null;

    public static void init() throws IOException {
        if (initialized) {return;}
        synchronized (MongoUtil.class) {
            if (initialized) {return;}
            config = Config.getInstance().getInstantPushConfig();
            mongoClient = new MongoClient(config.getMongoHost(), config.getMongoPort());
            initialized = true;
        }
    }

    public static void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
