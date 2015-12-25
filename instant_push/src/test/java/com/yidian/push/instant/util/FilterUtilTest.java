package com.yidian.push.instant.util;

import com.hipu.relevance.core.query.Query;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by tianyuzhi on 15/12/24.
 */
public class FilterUtilTest {

    private static final String dir = "target/filter_test";
    private static final String file = dir + "/queries";
    @BeforeClass
    public void init() throws IOException {
        FileUtils.forceMkdir(new File(dir));
        String str = "# start of finance\n" +
                "(ttl:开盘 OR ttl:午盘 OR ttl:收盘 OR ttl:收评 OR ttl:午评) AND (src:金融界 OR src:财新网 OR src:中金在线)\n" +
                "(ttl:抢筹 OR ttl:龙虎榜 OR ttl:机构 OR (ttl:青睐 AND ttl:股)) AND (src:金融界 OR src:东方财富网 OR src:华股财经)\n" +
                "# end of finance\n" +
                "# start of sports\n" +
                "# end of sports\n";
        FileUtils.writeStringToFile(new File(file), str);
    }

    @Test
    public void testLoad() {
        List<Query> queryList = FilterUtil.loadQueries(file);
        assert queryList.size() == 2;
    }

    @AfterClass
    public void clean() throws IOException {
        FileUtils.deleteDirectory(new File(dir));
    }
}