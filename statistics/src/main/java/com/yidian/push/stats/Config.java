package com.yidian.push.stats;

import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by yidianadmin on 15-2-1.
 */
@Getter
@Setter
public class Config {
    private String dailyStatMongoHost;
    private int dailyStatMongoPort;
    private String dailyStatMongoDB;
    private String dailyStatCollection;
    private String newsStatMongoHost;
    private int newsStatMongoPort;
    private String newsStatMongoDB;
    private String newsStatCollection;
    private String userStatMongoHost;
    private int userStatMongoPort;
    private String userStatMongoDB;
    private String userStatCollection;
    private String logBase = "/home/services/push_notification/log";
    private String mappingBase = "/home/services/push_notification/cache/userid_appid_mapping";
    private int mappingLookBackDays = 7;
    private int countLookBackDays = 2;

    private static String CONFIG_FILE = null;
    private static Config config;

    public static void setCONFIG_FILE(String configFile) {
        CONFIG_FILE = configFile;
    }

    public Config(){}
    public static Config getInstance() throws IOException {
        if (null != config) {
            return config;
        }
        synchronized (Config.class) {
            if (config == null) {
                String str = FileUtils.readFileToString(new File(CONFIG_FILE));
                config = GsonFactory.getDefaultGson().fromJson(str, Config.class);
            }
        }
        return config;
    }

    public static void dump() throws IOException {
        GsonFactory.getNonPrettyGson().toJson(Config.getInstance());
    }
}
