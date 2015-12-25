package com.yidian.push.instant.services;

import com.google.common.collect.ImmutableList;
import com.hipu.channel.analyzer.SegmentAnalyzer;
import com.hipu.channel.local.dao.DocDAO;
import com.hipu.channel.local.data.document.DocumentData;
import com.hipu.channel.local.data.document.DocumentField;
import com.hipu.relevance.core.analyzer.DefaultAnalyzer;
import com.hipu.relevance.core.query.Query;
import com.hipu.relevance.core.queryparser.ParseException;
import com.hipu.relevance.core.queryparser.QueryParser;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/12/23.
 */
public class ServiceTest {
    @Test
    public static void testMatching() throws ParseException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        File file = new File("service.properties");
        System.out.println(file.exists());
        List<DocumentData> datas = DocDAO.getInstance().getDocuments(ImmutableList.of("0BjSkADX"));
        QueryParser parser = new QueryParser(DocumentField.DEFAULT, new DefaultAnalyzer());
        String str = "(ttl:开盘 OR ttl:午盘 OR ttl:收盘 OR ttl:收评 OR ttl:午评) AND src:金融界";
        Query qry = parser.parse(str);
        for (DocumentData doc : datas) {
            System.out.println(qry.match(doc));
        }
    }

}