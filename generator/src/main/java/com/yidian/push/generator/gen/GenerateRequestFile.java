package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.data.Environment;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushChannel;
import com.yidian.push.generator.data.AppId;
import com.yidian.push.generator.data.NidLastPushTimePair;
import com.yidian.push.generator.util.PushRequestRedisUtil;
import com.yidian.push.generator.request.RequestManager;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.push_request.PushRequestStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.*;
import java.util.*;

/**
 * Created by tianyuzhi on 15/6/24.
 */
@Log4j
public class GenerateRequestFile {
    private static final LocalDateTime JAN_1_1970 = new LocalDateTime(1970, 1, 1, 0, 0);

    public static String genRequestFileName(String host, int port, String table, String pushType) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "_");
        String dateTime = DateTime.now().toString("yyyyMMddHHmmss");
        String name = new StringBuilder()
                .append(dateTime).append('-')
                .append(host.replaceAll("\\.", "_")).append('-')
                .append(port).append('-')
                .append(table.replaceAll("-", "_")).append('-')
                .append(pushType).append('.')
                .append(uuid).toString();
        return name;
    }

    public static int getNid(String table, int curNid, PushRecord pushRecord) {
        if (Platform.isAndroid(table) && AppId.XIAOMI.equals(pushRecord.getAppId())) {
            return (curNid + 1) % 2;
        }
        else {
            return (curNid + 1) % 5;
        }
    }


    /**
     * @param host
     * @param port
     * @param table
     * @param redisId
     * @param pushType
     * @param pushRecords
     * @param batchSize
     * @param protectMinutes
     * @return
     * @throws IOException
     */
    public static int generateRequestFile(String host, int port,
                                   String table, int redisId,
                                   String pushType, Collection<PushRecord> pushRecords,
                                   int batchSize, int protectMinutes) throws IOException {

//        uuid_str = str(uuid.uuid1()).replace('-', '_')
//        host = host.replace('.', '_')
//        table = table.replace('-', '_')
//        date_str = get_time_with_format("%Y%m%d%H%M%S")
//        preparing_file_base = "%s/%s-%s-%d-%s-%s.%s" % (preparing_dir, date_str, host, port, table, push_type, uuid_str)

        String preparingDir = PushRequestManager.getInstance().getRequestStatusDir(PushRequestStatus.PREPARING);
        RequestManager.forceMakeDir(preparingDir);
        String fileNameBase = genRequestFileName(host, port, table, pushType);
        int totalPushUsers = 0;

        Map<String, PushChannel> recordPushChannelMap = PushRequestRedisUtil.getPushChannel(redisId, table, pushRecords);
        Map<String, NidLastPushTimePair> recordNidLastPushTimeMap = PushRequestRedisUtil.getNidAndLastPushTime(redisId, table, pushRecords);
        List<PushRecord> validPushRecords = new ArrayList<>(pushRecords.size());
        Map<String, String> redisUpdateMap = new HashMap<>(pushRecords.size());

        int tableId = Platform.getTableId(table);
        boolean isIPhone = Platform.isIPhone(table);

        long curSeconds = System.currentTimeMillis() / 1000;
        long validLastPushTime = curSeconds - 60 * protectMinutes;

        for (PushRecord pushRecord : pushRecords) {
            String nidKey = PushRequestRedisUtil.genNidLastPushTimeKey(pushRecord, tableId);
            NidLastPushTimePair nidLastPushTimePair = recordNidLastPushTimeMap.get(nidKey);
            if (null == nidLastPushTimePair) {
                log.debug("filtered by nidLastPushTimePair: " + pushRecord.getUid());
                continue;
            }
            if (nidLastPushTimePair.lastPushTime > validLastPushTime) {
                log.debug("filtered by lastPushTime: " + pushRecord.getUid());
                continue;
            }
            int nextNid = getNid(table, nidLastPushTimePair.nid, pushRecord);
            if (!isIPhone) {
                pushRecord.setNid(nextNid);
            }
            String channelKey = pushRecord.getKey();
            pushRecord.setPushChannel(recordPushChannelMap.get(channelKey));
            validPushRecords.add(pushRecord);
            String redisUpdateKey = PushRequestRedisUtil.genNidLastPushTimeKey(pushRecord, tableId);
            String redisUpdateValue = new StringBuilder().append(nextNid).append(',').append(curSeconds).toString();
            redisUpdateMap.put(redisUpdateKey, redisUpdateValue);
        }
        int index = 0;
        int length = validPushRecords.size();
        while (index <validPushRecords.size()) {
            String preparingFile = new StringBuilder(preparingDir).append("/")
                    .append(fileNameBase).append(".offset.")
                    .append(index).append(".job").toString();
            PushRequest pushRequest = new PushRequest(preparingFile);
            long lastUserId = 0;
            int userCount = 0;
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(pushRequest.getFileName()));
                while (userCount < batchSize && index < length) {
                    PushRecord pushRecord = validPushRecords.get(index);
                    bw.write(pushRecord.toString());
                    bw.write('\n');
                    index ++;
                    if (lastUserId != pushRecord.getUid()) {
                        userCount ++;
                        totalPushUsers ++;
                        lastUserId = pushRecord.getUid();
                    }
                }
                bw.close();
                PushRequestManager.getInstance().markAsReady(pushRequest);
                log.info(pushRequest.getFileName() + " is ready to be processed.");
            } catch (IOException e) {
                log.error("write request file failed " + ExceptionUtils.getFullStackTrace(e));
            }
            finally {
                if (bw != null) {
                    try {bw.close();} catch (Exception e) {
                        log.error("close stream failed.." + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }
        }
        Environment environment = Config.getInstance().getGeneratorConfig().getEnvironment();
        if (null != environment && environment == Environment.RRODUCTION) {
            PushRequestRedisUtil.updateRedis(redisId, redisUpdateMap);
        }
        return totalPushUsers;
    }
}
