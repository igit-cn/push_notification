package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
import com.yidian.push.generator.data.PushIndex;
import com.yidian.push.generator.data.Range;
import com.yidian.push.generator.util.SqlUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyuzhi on 15/7/6.
 */
@Log4j
public class RefreshTokens {
    public static String FILED_SEPARATOR = "\u0001";
    private static RefreshTokens manager = null;

    private RefreshTokens() {
    }

    public static List<Range> genRanges(int start, int end, int rangeSize) {
        if (start > end || rangeSize <= 0) {
            return new ArrayList<>(0);
        }
        List<Range> ranges = new ArrayList<>((end-start)/rangeSize + 1);
        int index = start;
        while (index <= end) {
            final Range range = new Range(index, index + rangeSize);
            index += rangeSize;
            ranges.add(range);
        }
        return ranges;
    }

    public static RefreshTokens getInstance() {
        if (manager == null) {
            synchronized (RefreshTokens.class) {
                if (manager == null) {
                    manager = new RefreshTokens();
                }
            }
        }
        return manager;
    }

    public List<PushIndex> getCurrentIndexes() throws IOException {
        List<PushIndex> list = new ArrayList<>();
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        String basePath = config.getPushAllBaseDir();
        File[] subFiles = new File(basePath).listFiles();
        if (null != subFiles && subFiles.length > 0) {
            for (File file : subFiles) {
                if (file.isDirectory()) {
                    list.add(new PushIndex(file.getAbsolutePath()));
                }
            }
        }
        return list;
    }

    public static String getPathForHostTable(String basePath, HostPortDB hostPortDB, String table) {
        StringBuilder sb = new StringBuilder();
        sb.append(basePath).append('/')
                .append(hostPortDB.getHost()).append('-')
                .append(hostPortDB.getPort()).append('-')
                .append(hostPortDB.getDb()).append('-')
                .append(table);
        return sb.toString();
    }

    public PushIndex getNextIndex() throws IOException {
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        String timeStr = DateTime.now().toString("yyyy-MM-dd-HH-mm-ss");
        String nextPath = config.getPushAllBaseDir() + "/" + timeStr;
        return new PushIndex(nextPath, timeStr);
    }

    public void updateIndex(String latestIndex) throws IOException {
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        String indexFileName = config.getPushAllIndexFile();
        String tmpFile = indexFileName + ".tmp";
        FileUtils.writeStringToFile(new File(tmpFile), latestIndex);
        Files.move(new File(tmpFile).toPath(), new File(indexFileName).toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public PushIndex getLatestIndex() throws IOException {
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        String latestIndex = null;
        String indexFileName = config.getPushAllIndexFile();
        try {
            File indexFile = new File(indexFileName);
            if (indexFile.exists()) {
                latestIndex = FileUtils.readFileToString(indexFile).trim();
            }
        } catch (IOException e) {
            latestIndex = null;
        }
        return new PushIndex(latestIndex);

    }

    public void close(BufferedWriter writer) {
        if (null != writer) {
            try {
                writer.close();
            } catch (IOException e) {
                log.error("close failed " + ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    public void closeIgnoreException(Closeable obj) {
        try {
            if (null != obj) {
                obj.close();
            }
        } catch (Exception e) {
        }
    }


    public void refresh() throws IOException, SQLException {

        final GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        PushIndex latestIndex = getLatestIndex();
        PushIndex nextIndex = getNextIndex();
        boolean isSuccessful = true;

        String nextPath = nextIndex.getDataPath();
        FileUtils.forceMkdir(new File(nextPath));
        ExecutorService executorService = Executors.newFixedThreadPool(config.getAndroidThreadPoolSize() + config.getIPhoneThreadPoolSize());
        long startTime = System.currentTimeMillis();
        log.info("start to dump data from DB ");


        for (final HostPortDB hostPortDB : config.getMYSQL_HOSTS()) {
            for (Platform platform : Arrays.asList(Platform.IPHONE, Platform.ANDROID)) {
                final String table = platform.getTable();
                final String path = getPathForHostTable(nextPath, hostPortDB, table);
                FileUtils.forceMkdir(new File(path));

                final int rangeSize = config.getRangeSize(table);
                int maxUserId = config.getMaxUserId();
                List<Range> ranges = genRanges(0, maxUserId, rangeSize);

                for (final Range range : ranges) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            StringBuffer sql = new StringBuffer("select ").append(config.getPushAllSqlFields())
                                    .append(" from ").append(table)
                                    //.append(" where enable > 0 and appid in ").append(SqlUtil.genQuotedStringList(config.getAPPID_YIDIAN()))
                                    .append(" where enable > 0 ")
                                    .append(" and userid >= ").append(range.getStart()).append(" and userid < ").append(range.getEnd())
                                    .append(" order by userid ASC ");
                            //System.out.println(sql);
                            Connection connection = null;
                            Statement st = null;
                            ResultSet rs = null;
                            BufferedWriter bw = null;
                            try {
                                connection = MySqlConnectionPool.getConnection(hostPortDB);
                                connection.setAutoCommit(false);
                                st = connection.createStatement();
                                st.setFetchSize(config.getMysqlFetchSize());
                                st.executeQuery(sql.toString());
                                rs = st.getResultSet();
                                if (!rs.isBeforeFirst() && rs.getRow() == 0) {
                                    return;
                                }
                                int columnSize = rs.getMetaData().getColumnCount();
                                String file = new StringBuilder(path).append('/').append(range.getStart()).append('-').append(range.getEnd()).toString();
                                bw = new BufferedWriter(new FileWriter(file));
                                while (rs.next()) {
                                    boolean isFirst = true;
                                    StringBuilder line = new StringBuilder();
                                    for (int j = 1; j <= columnSize; j++) {
                                        if (!isFirst) {
                                            line.append(FILED_SEPARATOR);
                                        }
                                        isFirst = false;
                                        line.append(rs.getObject(j));
                                    }
                                    bw.write(line.toString());
                                    bw.write('\n');
                                }
                            } catch (Exception e) {
                                log.error("dump db failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                            } finally {
                                closeIgnoreException(bw);
                                if (null != bw) {
                                    try {
                                        bw.close();
                                    } catch (Exception e) {
                                    }
                                }
                                if (null != rs) {
                                    try {
                                        rs.close();
                                    } catch (Exception e) {
                                    }
                                }
                                if (null != st) {
                                    try {
                                        st.close();
                                    } catch (Exception e) {
                                    }
                                }
                                if (null != connection) {
                                    try {
                                        connection.close();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        log.info("dump the database with " + (endTime - startTime) + " mill seconds.");

        if (!isSuccessful) {
            FileUtils.forceDelete(new File(nextPath));
            log.info("dump failed");
        } else {
            updateIndex(nextPath);
            log.info("update the new index : " + nextPath);
            List<PushIndex> curIndexes = getCurrentIndexes();
            for (PushIndex pushIndex : curIndexes) {
                if (!pushIndex.isEmpty()
                        && !pushIndex.isInUse()
                        && pushIndex.isOlderThan(nextIndex)) {
                    log.info("delete the unneeded index: " + pushIndex.getDataPath());
                    FileUtils.forceDelete(new File(pushIndex.getDataPath()));
                }
            }
        }
        log.info("done");
    }
}
