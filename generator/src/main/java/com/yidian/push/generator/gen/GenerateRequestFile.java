package com.yidian.push.generator.gen;

import com.yidian.push.generator.request.RequestManager;
import com.yidian.push.generator.request.RequestStatus;
import com.yidian.push.push_request.PushRecord;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by tianyuzhi on 15/6/24.
 */
public class GenerateRequestFile {
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
    public static int generateRequestFile(String host, int port,
                                          String table, int redisId,
                                          String pushType, List<PushRecord> pushRecords,
                                          int batchSize, int protectMinutes) throws IOException {

//        uuid_str = str(uuid.uuid1()).replace('-', '_')
//        host = host.replace('.', '_')
//        table = table.replace('-', '_')
//        date_str = get_time_with_format("%Y%m%d%H%M%S")
//        preparing_file_base = "%s/%s-%s-%d-%s-%s.%s" % (preparing_dir, date_str, host, port, table, push_type, uuid_str)

        String preparingDir = RequestManager.getInstance().getRequestStatusDir(RequestStatus.READY);
        RequestManager.forceMakeDir(preparingDir);
        String fileNameBase = genRequestFileName(host, port, table, pushType);

        return 0;

    }
}
