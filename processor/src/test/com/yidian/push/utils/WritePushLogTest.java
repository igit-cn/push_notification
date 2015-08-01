package com.yidian.push.utils;

import com.yidian.push.data.HostPort;
import com.yidian.push.data.PushLog;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/28.
 */
public class WritePushLogTest {

    @Test
    public void testWriteLog() throws Exception {
        HostPort hostPort = new HostPort();
        hostPort.setHost("10.111.0.57");
        hostPort.setPort(18888);
        long userId = 3359602;
        List<PushLog.LogItem> logItemList = Arrays.asList(
                new PushLog.LogItem(DateTime.now().getMillis(), userId, "0A7oktXa", null, 2),
                new PushLog.LogItem(DateTime.now().getMillis(), userId, "0A7zLB6i", "u676", 8),
                new PushLog.LogItem(DateTime.now().getMillis(), userId, "l_0123125c0c5305fd264700060035", null, 64)
        );
        int connectionTimeout = 10;
        int readTimeout = 3;
        WritePushLog.writeLog(logItemList, hostPort, connectionTimeout, readTimeout);

    }
}