package com.yidian.push.stats;

import com.yidian.push.utils.DateUtil;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yidianadmin on 15-2-1.
 */

@Log4j
public class ImportData {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage : ImportData <config_file> <processing_day>  <file> [<file1> ...]");
            System.exit(-1);
        }
        String configFile = args[0];
        String processingDay = args[1];
        List<String> files = new LinkedList<>();
        for (int i = 2; i < args.length; i ++) {
            files.add(args[i]);
        }

        Config.setCONFIG_FILE(configFile);
        Config config = Config.getInstance();
        ExecutorService pool = Executors.newCachedThreadPool();
        log.info("start");
        for (String file : files) {
            pool.execute(new ImportFile(file, processingDay, config));
        }
        pool.shutdown();
        log.info("done");

    }
}
