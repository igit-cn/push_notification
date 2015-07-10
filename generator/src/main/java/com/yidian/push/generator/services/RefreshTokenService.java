package com.yidian.push.generator.services;

import com.yidian.push.config.Config;
import com.yidian.push.generator.gen.RefreshTokens;
import com.yidian.push.utils.FileLock;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by tianyuzhi on 15/7/7.
 */
public class RefreshTokenService {
    public static void main(String[] args) throws IOException, SQLException {
        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
            Config.setCONFIG_FILE("generator/src/main/resources/config/prod_config.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("generator/src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        //String lockFile = Config.getInstance().getGeneratorConfig().getLockFile();
//        if (!FileLock.lockInstance(lockFile)) {
//            System.out.println("One instance is already running, just quit.");
//            System.exit(1);
//        }
        RefreshTokens service = RefreshTokens.getInstance();
        service.refresh();
    }
}
