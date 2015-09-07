package com.yidian.push.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.PushHistoryConfig;
import com.yidian.push.data.HostPort;
import com.yidian.push.servlets.AddHistoryServlet;
import com.yidian.push.utils.FileLock;
import com.yidian.push.utils.ZionPoolUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;

/**
 * Created by yidianadmin on 14-8-10.
 */
public class Service implements Runnable {
    private static Logger logger = Logger.getLogger(Service.class);

    @Override
    public void run() {
        logger.info("Init Server");
        long startTime=System.currentTimeMillis();
        final Server server = new Server();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        try {
            PushHistoryConfig config = Config.getInstance().getPushHistoryConfig();
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        // for https
        try {
            PushHistoryConfig config = Config.getInstance().getPushHistoryConfig();
            for (HostPort hostPort : config.getHttpsHostPortList()) {
                SslSocketConnector sslConnector = new SslSocketConnector();
                SslContextFactory sslContextFactory = sslConnector.getSslContextFactory();
                sslContextFactory.setKeyStorePath(Service.class.getResource("/keystore.jks").toExternalForm());
                sslContextFactory.setKeyStorePassword("123456");
                sslContextFactory.setKeyManagerPassword("123456");
                sslConnector.setPort(hostPort.getPort());
                sslConnector.setHost(hostPort.getHost());
                server.addConnector(sslConnector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // init the connection to the redis servers
        try {
            ZionPoolUtil.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Stopping the service...");
                    ZionPoolUtil.destroy();
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
        //TODO: add authorization filter here.
        //root.addFilter(TestFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        root.setContextPath("/push_service");
        // set the max param size to java.lang.IllegalStateException: Form too large 337442>200000
        // at org.eclipse.jetty.server.Request.extractParameters(Request.java:352)
        try {
            root.setMaxFormContentSize(Config.getInstance().getPushHistoryConfig().getJettyMaxFormContentSize());
        } catch (IOException e) {
            logger.error("setMaxFormContentSize failed");
            throw new RuntimeException(e);
        }
        root.addServlet(new ServletHolder(new AddHistoryServlet()), "/add_history/*");

        HandlerList lists = new HandlerList();
        lists.setHandlers(new Handler[] {root});

        server.setHandler(lists);

        long endTime=System.currentTimeMillis();
        logger.info("Init complete, Cost : "+(endTime-startTime)+" ms");

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("init failed with error : " + ExceptionUtils.getFullStackTrace(e));
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException {

        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
            // Config.setCONFIG_FILE("generator/src/main/resources/config/prod_config.json");
            Config.setCONFIG_FILE("push_history/src/main/resources/config/config.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("generator/src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        String lockFile = Config.getInstance().getPushHistoryConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        Service service = new Service();
        service.run();
    }
}
