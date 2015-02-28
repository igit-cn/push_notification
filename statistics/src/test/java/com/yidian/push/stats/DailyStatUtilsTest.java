package com.yidian.push.stats;

import com.yidian.push.data.Platform;
import com.yidian.push.utils.DateUtil;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static org.testng.Assert.*;

public class DailyStatUtilsTest {
    private static final String BASE = "target/push_notification_daily_stat";
    @BeforeClass
    public void before() throws IOException {
        FileUtils.forceMkdir(new File(BASE));
    }
    @AfterClass
    public void after() throws IOException {
        FileUtils.deleteDirectory(new File(BASE));
    }

    @Test
    public void testGetBucketId() throws Exception {
        int userId = 23;
        assert 3 == DailyStatUtils.getBucketId(23);

    }

    @Test
    public void testGenDailyStatKeys() throws Exception {

    }

    @Test
    public void testGetLogFile() throws Exception {

    }

    @Test
    public void testGetLogIndex() throws Exception {
        String day = "2015-01-01";
        Platform platform = Platform.ANDROID;
        String real = DailyStatUtils.getLogIndex(BASE, day, platform);
        String expected = BASE + "/" + platform.toString() + "/" + day  + ".index";
        assert expected.equals(real);

    }

    @Test
    public void testGetPushUsers() throws Exception {
        String day = DateUtil.dateToYYYY_MM_DD(new Date());
        String file = BASE + "/" + day + ".index";
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        int[] offsets = {0, 1, 2, 3, 4, 0, 6};
        for (int offset : offsets) {
            out.writeInt(offset);
        }
        out.close();
        Set<Integer> set = DailyStatUtils.getPushUsers(file);
        for (int i : set) {
            System.out.println(i);
        }
    }

    @Test
    public void testGetMappingFile() throws Exception {
        String base = "/tmp";
        String day = "2015-01-01";
        String expected = base + "/uid2appid." + day;
        String real = DailyStatUtils.getMappingFile(base, day);
        assert expected.equals(real);
    }

    @Test
    public void testGetLatestAvailableMappingFile() throws Exception {
        String[] days = {DateUtil.dateToYYYY_MM_DD(DateUtil.incrDate(new Date(), -2)),DateUtil.dateToYYYY_MM_DD(DateUtil.incrDate(new Date(), -3))
        };
        for (String day: days) {
            String file = BASE + "/uid2appid." + day;
            FileUtils.touch(new File(file));
        }
        String expected = BASE + "/uid2appid." + DateUtil.dateToYYYY_MM_DD(DateUtil.incrDate(new Date(), -2));
        String real = DailyStatUtils.getLatestAvailableMappingFile(BASE, 7);
        assert expected.equals(real);
    }
}