package com.yidian.push.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.PushHistoryConfig;
import com.yidian.push.config.RecommendGeneratorConfig;
import com.yidian.push.data.HostPort;
import com.yidian.push.recommend_gen.Generator;
import com.yidian.push.servlets.GetRunningInstances;
import com.yidian.push.servlets.PushRecommend;
import com.yidian.push.servlets.SlowGenerator;
import com.yidian.push.utils.FileLock;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Service implements Runnable {
    private static volatile boolean keepRunning = false;
    @Override
    public void run() {
        RecommendGeneratorConfig config = null;
        try {
            config = Config.getInstance().getRecommendGeneratorConfig();
            log.info("generatorConfig is " + GsonFactory.getPrettyGson().toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Server server = new Server();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(100);
        threadPool.setMaxThreads(200);
        server.setThreadPool(threadPool);

        for (HostPort hostPort : config.getHostPortList()) {
            Connector conn = new SelectChannelConnector();
            conn.setHost(hostPort.getHost());
            conn.setPort(hostPort.getPort());
            server.addConnector(conn);
        }
        server.setGracefulShutdown(1000);

        try {
            HttpConnectionUtils.init(config.getHttpConnectionMaxTotal(), config.getHttpConnectionDefaultMaxPerRoute());
        } catch (IOException e) {
            e.printStackTrace();
        }


        ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
        //TODO: add authorization filter here.
        //root.addFilter(TestFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        root.setContextPath("/push_service");
        // set the max param size to java.lang.IllegalStateException: Form too large 337442>200000
        // at org.eclipse.jetty.server.Request.extractParameters(Request.java:352)
        root.addServlet(new ServletHolder(new SlowGenerator()), "/slow_generator/*");
        root.addServlet(new ServletHolder(new PushRecommend()), "/push_recommend/*");
        root.addServlet(new ServletHolder(new GetRunningInstances()), "/get_running/*");

        HandlerList lists = new HandlerList();
        lists.setHandlers(new Handler[] {root});

        server.setHandler(lists);
        try {
            server.start();
            log.info("server started...");
        } catch (Exception e) {
            log.error("could not start the services" + ExceptionUtils.getFullStackTrace(e));
        }


        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepRunning = false;
                log.info("receive kill signal ...");
                try {
                    server.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    server.join();
                } catch (Exception e) {
                    log.error("", e);
                }

                log.info("Server exit safely!");
            }
        });


    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
           // Config.setCONFIG_FILE("generator/src/main/resources/config/prod_config.json");
            Config.setCONFIG_FILE("recommend_generator/src/main/resources/config/config.json");
            System.setProperty("log4j.configuration", "recommend_generator/src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("recommend_generator/src/main/resources/config/log4j_debug.properties");
           // Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        System.out.println(GsonFactory.getDefaultGson().toJson(Config.getInstance().getRecommendGeneratorConfig()));
        String lockFile = Config.getInstance().getRecommendGeneratorConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        new Service().run();
    }
}
