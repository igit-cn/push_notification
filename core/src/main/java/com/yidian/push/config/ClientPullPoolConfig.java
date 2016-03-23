package com.yidian.push.config;

import com.yidian.push.data.HostPort;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.config.RequestConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tianyuzhi on 16/3/21.
 */
@Getter
@Setter
public class ClientPullPoolConfig {
    private String lockFile = "";
    private List<HostPort> hostPortList = Arrays.asList(new HostPort("localhost", 8080));
    private List<HostPort> httpsHostPortList = Arrays.asList(new HostPort("localhost", 8081));
    private int jettyMinThreads = 50;
    private int jettyMaxThreads = 100;
    private int jettyMaxFormContentSize = 20 * 1024 * 1024; // 20M
    private String docInfoURL = "http://a1.go2yd.com/Website/contents/content";
    private String newsPoolURL = "http://pandora.yidian-inc.com/tools/newspool/getlist";
    private int newsPoolBatch = 100;
    private int newsPoolInitSize = 1000;
    private int newsMaxPoolSize = 10000;
    private int refreshPeriodInSeconds = 60;
    private int socketConnectTimeoutInSeconds = 10;
    private int socketReadTimeoutInSeconds = 3;
    private int userNewsSize = 5;
    private int logRetainDays = 5;
    private boolean writeAccessLog = true;

    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(socketConnectTimeoutInSeconds * 1000)
                .setConnectionRequestTimeout(socketConnectTimeoutInSeconds * 1000)
                .setSocketTimeout(socketReadTimeoutInSeconds * 1000).build();
    }
}
