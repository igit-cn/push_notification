package com.yidian.push.client_pull.services;

import com.yidian.push.client_pull.servlets.GetPushPoolServlet;
import com.yidian.push.config.ClientPullPoolConfig;
import com.yidian.push.config.Config;
import com.yidian.push.data.HostPort;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.io.IOException;

/**
 * Created by yidianadmin on 14-8-10.
 */
public class Service implements Runnable {
    private static Logger logger = Logger.getLogger(Service.class);

    @Override
    public void run() {
        logger.info("Init Server");
        long startTime = System.currentTimeMillis();
        final Server server = new Server();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        ClientPullPoolConfig config = null;
        try {
            config = Config.getInstance().getClientPullPoolConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (HostPort hostPort : config.getHostPortList()) {
            Connector conn = new SelectChannelConnector();
            conn.setHost(hostPort.getHost());
            conn.setPort(hostPort.getPort());
            server.addConnector(conn);
        }
        threadPool.setMinThreads(config.getJettyMinThreads());
        threadPool.setMaxThreads(config.getJettyMaxThreads());
        server.setGracefulShutdown(1000);
        server.setThreadPool(threadPool);

        // servlets to handle
        GetPushPoolServlet getPushPoolServlet = new GetPushPoolServlet(config);


        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Stopping the service...");
                    server.stop();
                } catch (Exception e) {
                    logger.error("", e);
                }
                try {
                    server.join();
                } catch (Exception e) {
                    logger.error("", e);
                }

                logger.info("Server exit safely!");
            }
        }));


        ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
        root.setContextPath("/push_service");
        root.addServlet(new ServletHolder(getPushPoolServlet), "/getPushPool/*");

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(root);

        HandlerCollection handlers = new HandlerCollection();
        if (config.isWriteAccessLog()) {
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            String logPath = System.getenv("jetty.logs");
            if (logPath == null) {
                logPath = "logs";
            }
            logger.info("logs dir:" + logPath);
            try {
                FileUtils.forceMkdir(new File(logPath));
            } catch (IOException e) {
                logger.error("could not create log dir ");
            }
            NCSARequestLog requestLog = new NCSARequestLog(logPath + "/jetty-yyyy_MM_dd.request.log");
            requestLog.setFilenameDateFormat("yyyy_MM_dd");
            requestLog.setRetainDays(config.getLogRetainDays());
            requestLog.setAppend(true);
            requestLog.setExtended(false);
            requestLog.setLogTimeZone("GMT+8");
            requestLogHandler.setRequestLog(requestLog);

            contexts.addHandler(requestLogHandler);
            handlers.setHandlers(new Handler[]{contexts, new DefaultHandler(), requestLogHandler});
        }
        else {
            handlers.setHandlers(new Handler[]{contexts, new DefaultHandler()});
        }
        server.setHandler(handlers);


        long endTime = System.currentTimeMillis();
        logger.info("Init complete, Cost : " + (endTime - startTime) + "ms");

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("init failed");
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
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
        Service service = new Service();
        service.run();
    }
}
