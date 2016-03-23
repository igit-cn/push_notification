package com.yidian.push.config;

import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by yidianadmin on 15-1-12.
 */
@Setter
@Getter
public class Config {
    // fields
    private String lockFile = null ;
    private String pushRequestBaseDir = "";
    private String requestBaseDir = "";
    private GeneratorConfig generatorConfig = null;
    private ProcessorConfig processorConfig = null;
    private LoggingConfig loggingConfig = null;
    private PushHistoryConfig pushHistoryConfig = null;
    private RecommendGeneratorConfig recommendGeneratorConfig = null;
    private RecommendGeneratorOnlineConfig recommendGeneratorOnlineConfig = null;
    private InstantPushConfig instantPushConfig = null;
    private WeatherPushConfig weatherPushConfig = null;
    private ClientPullPoolConfig clientPullPoolConfig = null;
    //
    private static String CONFIG_FILE = null;
    private static Config config;

    public static void setCONFIG_FILE(String configFile) {
        CONFIG_FILE = configFile;
    }

    private Config(){}
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
}
