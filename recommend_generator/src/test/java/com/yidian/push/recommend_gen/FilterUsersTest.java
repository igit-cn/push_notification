package com.yidian.push.recommend_gen;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/12/9.
 */
public class FilterUsersTest {
    String location = "target/FilterUsersTest";

    @BeforeClass
    public void init() throws IOException {
        FileUtils.forceMkdir(new File(location));
        System.out.println(new File(location).getAbsolutePath());
        FileUtils.writeStringToFile(new File(location + "/2015-11-05"), "");
        FileUtils.writeStringToFile(new File(location + "/1.2015-11-05"), "");
        FileUtils.writeStringToFile(new File(location + "/2.2015-11-05"), "");
        FileUtils.writeStringToFile(new File(location + "/3.2015-11-04"), "");
    }
    @AfterClass
    public void clean() throws IOException {
        FileUtils.forceDeleteOnExit(new File(location));
    }

    @Test
    public void testGetMatchFiles() throws Exception {
        String glob = "*2015-11-05";
        List<String> files = FilterUsers.getMatchFiles(glob, location);
        System.out.println(files);


    }
}