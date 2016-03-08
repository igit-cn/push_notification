package com.yidian.push.instant.util;

import com.hipu.channel.local.dao.DocDAO;
import com.hipu.channel.local.data.document.DocumentData;
import com.hipu.channel.local.data.document.DocumentField;
import com.hipu.relevance.core.analyzer.DefaultAnalyzer;
import com.hipu.relevance.core.query.Query;
import com.hipu.relevance.core.queryparser.ParseException;
import com.hipu.relevance.core.queryparser.QueryParser;
import com.yidian.push.instant.data.DocChannelInfo;
import lombok.extern.log4j.Log4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by tianyuzhi on 15/12/24.
 */
@Log4j
public class FilterUtil {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String COMMENT_PREFIX = "#";
    private static final String START_TAG = "START_TAG";
    private static final String END_TAG = "END_TAG";
    private static final String DEFAULT_TAG = "test_tag";


    public static List<Query> loadQueries(String file) {
        List<Query> queryList = new ArrayList<>();
        Path filePath = new File(file).toPath();
        BufferedReader reader = null;
//        reader = Files.newBufferedReader(filePath, UTF_8);
        QueryParser parser = new QueryParser(DocumentField.DEFAULT, new DefaultAnalyzer());
        try {
            reader = Files.newBufferedReader(filePath, UTF_8);
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(COMMENT_PREFIX)
                        || line.startsWith(START_TAG)
                        || line.startsWith(END_TAG)) {
                    continue;
                }
                try {
                    Query query = parser.parse(line);
                    queryList.add(query);
                } catch (ParseException e) {
                    log.error("could not parse query: " + line);
                }
            }
            log.info("load " + queryList.size() + " queries from " + file);

        } catch (IOException e) {
            log.error("could not read data from file: " + file);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return queryList;
    }

    public static List<DocChannelInfo> matchQueries(List<Query> queryList, List<DocChannelInfo> docChannelInfoList) {
        if (docChannelInfoList == null || queryList == null) {
            return new ArrayList<>(0);
        }
        List<DocChannelInfo> matchedList = new ArrayList<>(docChannelInfoList.size());
        Map<String, DocChannelInfo> docIdMapping = new HashMap<>(docChannelInfoList.size());
        for (DocChannelInfo docChannelInfo : docChannelInfoList) {
            docIdMapping.put(docChannelInfo.getDocId(), docChannelInfo);
        }
        List<DocumentData> documentDataList = DocDAO.getInstance().getDocuments(docIdMapping.keySet());
        for (DocumentData documentData : documentDataList) {
            log.info("FILTER docid:" + documentData.getDocid());
            for (Query query : queryList) {
                if (query.match(documentData)) {
                    matchedList.add(docIdMapping.get(documentData.getDocid()));
                    log.info("MATCHED:" + documentData.getDocid());
                    break;
                }
            }
        }
        return matchedList;
    }

    public static Map<String, List<Query>> loadTagQueries(String file) {
        Map<String, List<Query>> tagToQueriesMap = new HashMap<>();
        List<Query> queryList;
        Path filePath = new File(file).toPath();
        BufferedReader reader = null;
        QueryParser parser = new QueryParser(DocumentField.DEFAULT, new DefaultAnalyzer());
        try {
            reader = Files.newBufferedReader(filePath, UTF_8);
            String line = null;
            String curTag = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(COMMENT_PREFIX)
                        || line.isEmpty()) {
                    continue;
                }
                if (line.startsWith(START_TAG)) {
                    String[] arr = line.split(" ", 2);
                    if (arr.length >= 2) {
                        curTag = arr[1];
                    }
                }
                else if (line.startsWith(END_TAG)) {
                    curTag = DEFAULT_TAG;
                } else {
                    if (!tagToQueriesMap.containsKey(curTag)) {
                        tagToQueriesMap.put(curTag, new ArrayList<>());
                    }
                    try {
                        Query query = parser.parse(line);
                        queryList = tagToQueriesMap.get(curTag);
                        queryList.add(query);
                    } catch (ParseException e) {
                        log.error("could not parse query: " + line);
                    }
                }
            }
            for (String tag : tagToQueriesMap.keySet()) {
                log.info("load " + tagToQueriesMap.get(tag).size() + " " + tag + " queries from file " + file);
            }

        } catch (IOException e) {
            log.error("could not read data from file: " + file);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return tagToQueriesMap;
    }

    public static List<DocChannelInfo> matchQueries(Map<String, List<Query>> tagToQueryList, List<DocChannelInfo> docChannelInfoList) {
        if (docChannelInfoList == null || tagToQueryList == null || tagToQueryList.size() == 0) {
            return new ArrayList<>(0);
        }
        List<DocChannelInfo> matchedList = new ArrayList<>(docChannelInfoList.size());
        Map<String, DocChannelInfo> docIdMapping = new HashMap<>(docChannelInfoList.size());
        for (DocChannelInfo docChannelInfo : docChannelInfoList) {
            docIdMapping.put(docChannelInfo.getDocId(), docChannelInfo);
        }
        List<DocumentData> documentDataList = DocDAO.getInstance().getDocuments(docIdMapping.keySet());
        for (DocumentData documentData : documentDataList) {
            log.info("FILTER docid:" + documentData.getDocid());
            for (String tag : tagToQueryList.keySet()) {
                for (Query query : tagToQueryList.get(tag)) {
                    if (query.match(documentData)) {
                        DocChannelInfo matchedItem = docIdMapping.get(documentData.getDocid());
                        matchedItem.setDocDate(documentData.getDate());
                        matchedItem.addTag(tag);
                        matchedItem.setSrc(documentData.getDsource());
                        matchedList.add(matchedItem);
                        log.info(tag + " MATCHED:" + documentData.getDocid());
                        break;
                    }
                }
            }
        }
        return matchedList;
    }

    public static List<DocChannelInfo> filterByDocTime(List<DocChannelInfo> docChannelInfoList, long filterDocTimeInSeconds) {
        if (null == docChannelInfoList) {
            return new ArrayList<>(0);
        }
        List<DocChannelInfo> res = new ArrayList<>(docChannelInfoList.size());
        Date now = new Date();
        for (DocChannelInfo docChannelInfo : docChannelInfoList) {
            Date docDate = docChannelInfo.getDocDate();
            long gapSeconds = (now.getTime() - docDate.getTime()) / 1000;
            if (gapSeconds <= filterDocTimeInSeconds) {
                res.add(docChannelInfo);
            }
            else {
                log.info("FILTER_BY_DOC_TIME:" + docChannelInfo.getDocId() + ",docDate["
                        + new DateTime(docChannelInfo.getDocDate(), DateTimeZone.UTC).toString("yyyy-MM-dd HH:mm:ss")
                + "],todayDate["+ new DateTime(DateTimeZone.UTC).toString("yyyy-MM-dd HH:mm:ss") +"]");
            }
        }
        return res;
    }
}
