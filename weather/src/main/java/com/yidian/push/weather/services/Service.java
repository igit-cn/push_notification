package com.yidian.push.weather.services;

import com.google.common.collect.Maps;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.jdk.JDKAsyncHttpProvider;
import com.yidian.push.config.Config;
import com.yidian.push.config.WeatherPushConfig;
import com.yidian.push.utils.FileLock;
import com.yidian.push.utils.GsonFactory;
import com.yidian.serving.metrics.MetricsFactory;
import com.yidian.serving.metrics.MetricsFactoryUtil;
import com.yidian.serving.metrics.OnDemandMetricsFactory;
import com.yidian.serving.metrics.reporter.opentsdb.HttpOpenTsdbClient;
import com.yidian.serving.metrics.reporter.opentsdb.OpenTsdbClient;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Service implements Runnable {
    private static volatile boolean keepRunning = false;

    @Override
    public void run() {
        WeatherPushConfig config = null;

        try {
            config = Config.getInstance().getWeatherPushConfig();
            log.info("Config is " + GsonFactory.getPrettyGson().toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            keepRunning = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("push notification logging init ...");
//            throw new RuntimeException(e);
//        }

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepRunning = false;
                log.info("receive kill signal ...");
                try {
                    currentThread.join();
                    log.info("shutdown the thread pools");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //init the reporter
        String opentsdbAddress = config.getOpentsdbAddress();
        Map tags = config.getOpentsdbTags();
        if (null == tags || tags.size() == 0) {
            tags = Maps.newHashMap();
        }
        try {
            tags.put("host", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            log.error("could not get the host name");
        }

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new JDKAsyncHttpProvider(new AsyncHttpClientConfig.Builder().build()));
        OpenTsdbClient openTsdbClient = new HttpOpenTsdbClient(asyncHttpClient, opentsdbAddress);
        MetricsFactory metricsFactory = new OnDemandMetricsFactory(tags, openTsdbClient);
        MetricsFactoryUtil.register(metricsFactory);

        log.info("service started.");

    }

    public static void main(String[] args) throws IOException {
        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
            Config.setCONFIG_FILE("src/main/resources/config/config.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        String lockFile = Config.getInstance().getWeatherPushConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        Service service = new Service();
        service.run();

    }

}
