package com.yidian.push.data;

import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 15/6/16.
 */
@Setter
@Getter
public class RedisHostPort {
    int id;
    String host;
    int port;
    String db;

    public RedisHostPort(int id, String host, int port, String db) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.db = db;
    }

    public RedisHostPort() {
        this.id = 0;
        this.host = "localhost";
        this.port = 6379;
        this.db = "0";
    }

    public String toJson() {
        return GsonFactory.getNonPrettyGson().toJson(this);
    }
}
