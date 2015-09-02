package com.yidian.push.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 15/7/28.
 */
@Getter
@Setter
public class HostPort {
    private String host;
    private int port;

    public HostPort(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
