package com.yidian.push.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.RecommendGeneratorConfig;
import com.yidian.push.recommend_gen.Generator;
import com.yidian.push.utils.FileLock;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Service implements Runnable {
    private static volatile boolean keepRunning = false;

    public String getInputFile(RecommendGeneratorConfig config)
    {
        String inputPath = config.getInputDataPath();
        int lookBackDay = config.getInputLookBackDays();
        for (int i = 0; i < lookBackDay; i ++) {
            String day = DateTime.now().minusDays(i).toString("yyyy-MM-dd");
            String fileName = inputPath + "/" + day;
            File file = new File(fileName);
            if (file.exists() && file.isFile()) {
                log.info("get input file :" + fileName);
                return fileName;
            }
            else {
                log.info("input file :" + fileName + " does not exist");
            }
        }
        return null;
    }

    public String getOutputPath(RecommendGeneratorConfig config) {
        String outputPath = config.getOutputDataPath();
        String day = DateTime.now().toString("yyyy-MM-dd");
        int times = config.getOutputLookBackTimes();
        for (int i = 0; i < times; i ++) {
            String dirName = outputPath + "/" + day + "_" + i;
            File file = new File(dirName);
            if (!file.exists()) {
                log.info("get output dir:" + dirName);
                return dirName;
            }
            else {
                log.info("output dir " + dirName + " already exists");
            }
        }
        return null;
    }

    public void gen() throws IOException, InterruptedException {
        RecommendGeneratorConfig config = Config.getInstance().getRecommendGeneratorConfig();
        HttpConnectionUtils.init(config.getHttpConnectionMaxTotal(), config.getHttpConnectionDefaultMaxPerRoute());

        Generator generator = new Generator();
        String inputFile = getInputFile(config);
        String outputPath = getOutputPath(config);
        log.info("input file:[" + inputFile + "], output dir:[" + outputPath + "]");
        if (StringUtils.isEmpty(inputFile) || StringUtils.isEmpty(outputPath)) {
            log.info("invalid input file or output path");
            generator.clear();
            //throw new RuntimeException("invalid input file or output path");
        }
        else {
            generator.processFile(inputFile, outputPath);
        }
    }

    @Override
    public void run() {
        RecommendGeneratorConfig config = null;
        try {
            config = Config.getInstance().getRecommendGeneratorConfig();
            log.info("generatorConfig is " + GsonFactory.getPrettyGson().toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }


//        try {
//
//            keepRunning = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("push notification generator init ...");
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
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
//        executor.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                } catch (Exception e) {
//                    log.error("refresh tokens failed with exception : " + ExceptionUtils.getFullStackTrace(e));
//                }
//            }
//        }, 0, generatorConfig.getRefreshTokenFrequencyInSeconds(), TimeUnit.SECONDS);

//
//        while(keepRunning) {
//            try {
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e) {
//                log.error("sleep failed...");
//            }
//        }
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
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("recommend_generator/src/main/resources/config/log4j_debug.properties");
           // Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        System.out.println(GsonFactory.getDefaultGson().toJson(Config.getInstance().getRecommendGeneratorConfig()));
        String lockFile = Config.getInstance().getRecommendGeneratorConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        new Service().gen();
//        Service service = new Service();
//        service.run();
    }
}
