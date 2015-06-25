package com.yidian.push.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yidianadmin on 15-3-10.
 */
@Getter
@Setter
public class HostPortDB {
    String host;
    int port;
    String db;

    public HostPortDB() {
        this.host = "localhost";
        this.port = 3306;
        this.db = "db";
    }
    public HostPortDB(String host, int port, String db) {
        this.host = host;
        this.port = port;
        this.db = db;
    }

    public String toString() {
        return new StringBuilder(host).append("_").append(port).append("_").append(db).toString();
    }

    public String getMysqlUrl() {
        //("jdbc:mysql://10.111.0.70:3306/account");
        return new StringBuilder("jdbc:mysql://").append(host).append(":").append(port).append("/").append(db).toString();
    }
}
