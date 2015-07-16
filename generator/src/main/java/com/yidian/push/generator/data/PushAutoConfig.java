package com.yidian.push.generator.data;

import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
import com.yidian.push.generator.util.SqlUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Getter
@Setter
public class PushAutoConfig {
    Task task;
    HostPortDB hostPortDB;
    transient Map<Long, String> userIdChannelMapping = null;
    Set<Integer> bucketIds = null;
    Range userRange = null;
    int batchSize = 10000;
    long todayFirstUserId = 0;
    String file;

    public PushAutoConfig() {
    }

}
