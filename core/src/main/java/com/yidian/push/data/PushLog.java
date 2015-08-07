package com.yidian.push.data;

import com.yidian.push.utils.ByteUtil;
import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by yidianadmin on 14-8-20.
 */
@Getter
@Setter
public class PushLog {
    public static int UID_SEGMENT = 4;
    public static int LOG_SEGMENT_OLD = 256;
    public static String LOG_SEGMENT_OLD_DATE = "2014-08-19";
    public static int LOG_SEGMENT = 512;
    public static String DOC_ID_PREFIX = "news_";
    public static String LIST_PREFIX = "l_";
    public static int DOC_ID_DATA_LENGTH = 16;
    public static int OLD_MAX_USER_ID = 30000000;
    public static String USER_ID_OLD_DATE = "2015-01-07";
    public static int MAX_USER_ID = 60000000;

    private static Logger logger = Logger.getLogger(PushLog.class);

    // format_1: [data_len:0][push_type:1][push_date:2..6][userid:6..10][docid:10..26][newsChannel:26:42] = 42 bytes
    // format_2: [data_len0][push_type:1][push_date:2..6][userid:6..10][docid:10..26][newsChannel:26:30] = 30 bytes
    // format_3: [data_len:0][push_type:1][push_date:2..6][userid:6..10][docid:10..26] = 26 bytes

    private int len;
    private int pushType;
    private long pushDate;
    private int userId;
    private String docId;
    private String channel;


    public PushLog(int len, int pushType, long pushDate, int userId, String docId, String channel) {
        this.len = len;
        this.pushType = pushType;
        this.pushDate = pushDate;
        this.userId = userId;
        this.docId = docId;
        this.channel = channel;
    }


    public static int getUidSegmentSize(String date) {
        return UID_SEGMENT;
    }

    public static int getPushLogSegmentSize(String date) {
        if (LOG_SEGMENT_OLD_DATE.compareTo(date) < 0) {
            return LOG_SEGMENT;
        } else {
            return LOG_SEGMENT_OLD;
        }
    }

    public static int getMaxUserId(String date) {
        if (USER_ID_OLD_DATE.compareTo(date) < 0) {
            return MAX_USER_ID;
        }
        else {
            return OLD_MAX_USER_ID;
        }

    }

    public static String decodeDocId(byte[] bytes, int start, int end) {
        String docId = "";
        if ((null != bytes) && (end-start == DOC_ID_DATA_LENGTH)) {
            if (bytes[start] == '#' && (bytes[start+1] == 'L' || bytes[start+1] == 'D')) {
                int realIndex = start;
                for (int i = start+2; i < end; i ++) {
                    if (bytes[i] != '#') {
                        realIndex = i;
                        break;
                    }
                }
                if (realIndex > start && realIndex < end){
                    if (bytes[start+1] == 'D') {
                        docId = (new String(bytes)).substring(realIndex, end);
                    }
                    else if (bytes[start+1] == 'L') {
                        docId = LIST_PREFIX + ByteUtil.byte2HexStr(bytes, realIndex, end);
                    }
                }
                else {
                    docId = DOC_ID_PREFIX + ByteUtil.byte2HexStr(bytes, start, end);
                }
            }
            else {
                docId = DOC_ID_PREFIX + ByteUtil.byte2HexStr(bytes, start, end);
            }
        }
        return docId;
    }

    public static String decodeDocId(byte[] bytes) {
        String docId = "";
        if ((null != bytes) && (bytes.length == DOC_ID_DATA_LENGTH)) {
            if (bytes[0] == '#' && (bytes[1] == 'L' || bytes[1] == 'D')) {
                int realIndex = 0;
                for (int i = 2; i < bytes.length; i ++) {
                    if (bytes[i] != '#') {
                        realIndex = i;
                        break;
                    }
                }
                if (realIndex > 1 && realIndex < bytes.length){
                    if (bytes[1] == 'D') {
                        docId = (new String(bytes)).substring(realIndex);
                    }
                    else if (bytes[1] == 'L') {
                        docId = LIST_PREFIX + ByteUtil.byte2HexStr(bytes, realIndex, bytes.length);
                    }
                }
                else {
                    docId = DOC_ID_PREFIX + ByteUtil.byte2HexStr(bytes, 0, bytes.length);
                }
            }
            else {
                docId = DOC_ID_PREFIX + ByteUtil.byte2HexStr(bytes, 0, bytes.length);
            }
        }
        return docId;
    }

    public static String genAppend(char ch, int times) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < times; i ++) {
            stringBuilder.append(ch);
        }
        return stringBuilder.toString();
    }

    public static byte[] encodeDocId(String docStr) {
        byte[] bytes = null;
        if (docStr.startsWith(DOC_ID_PREFIX)) {
            bytes = ByteUtil.hexStr2Bytes(docStr.substring(DOC_ID_PREFIX.length()));
        }
        else if (docStr.startsWith(LIST_PREFIX) && docStr.length() > LIST_PREFIX.length() && (docStr.length() - LIST_PREFIX.length()) % 2 == 0) {
            String append = genAppend('#', DOC_ID_DATA_LENGTH - 2 - (docStr.length() - LIST_PREFIX.length())/2);
            byte[] encodedBytes = ByteUtil.hexStr2Bytes(docStr.substring(LIST_PREFIX.length()));
            bytes = new byte[DOC_ID_DATA_LENGTH];
            byte[][] byteArray = {"#L".getBytes(), append.getBytes(), encodedBytes};
            int index = 0;
            for (byte[] tmp_bytes: byteArray) {
                for (byte by : tmp_bytes) {
                    if (index < bytes.length) {
                        bytes[index++] = by;
                    }
                }
            }
        }
        else if (docStr.length() >= 8 && docStr.length() <= 14) {
            bytes = new StringBuilder("#D")
                    .append(genAppend('#', DOC_ID_DATA_LENGTH - 2 - docStr.length()))
                    .append(docStr)
                    .toString().getBytes();
        } else {
            String str = genAppend('f', DOC_ID_DATA_LENGTH);
            bytes = str.getBytes();
        }
        return bytes;
    }

    public static List<PushLog> decode(byte[] bytes) {
        List<PushLog> list = new ArrayList<>();
        if (null != bytes && bytes.length > 0) {
            int index = 0;

            while (index < bytes.length && bytes[index] != '\0') {
                int len = (int) bytes[index];
                int pushType = ByteUtil.toInt(bytes[index + 1]);
                long pushDate = ByteUtil.unsigned4BytesToInt(bytes, index + 2);
                int userId = (int) ByteUtil.unsigned4BytesToInt(bytes, index + 6);
                //String docId = decodeDocId(ByteUtil.subBytes(bytes, index + 10, index + 26));
                String docId = decodeDocId(bytes, index+10, index+26);
                String channel = null;
                if (len == 42) {
                    channel = ByteUtil.byte2HexStr(bytes, index + 26, index + len);
                } else if (len == 30) {
                    int channelId = Integer.valueOf(ByteUtil.byte2HexStr(bytes, index + 26 + 1, index + 30), 16);
                    if (channelId < 10000 && bytes[index + 26] == 's') {
                        channel = "sc" + channelId;
                    } else {
                        channel = (char) bytes[index + 26] + String.valueOf(channelId);
                    }
                }
                list.add(new PushLog(len, pushType, pushDate, userId, docId, channel));
                index += len;
            }
        }
        return list;
    }

    public static List<PushLog> getLog(String logBaseDir, String platform, String day, int userId) throws IOException {
        String index = new StringBuilder().append(logBaseDir).append("/").append(platform).append("/").append(day).append(".index").toString();
        String data = new StringBuilder().append(logBaseDir).append("/").append(platform).append("/").append(day).append(".data").toString();
        File indexFile = new File(index);
        File dataFile = new File(data);
        List<PushLog> list = new ArrayList<>();
        if (userId > getMaxUserId(day)) {
            return list;
        }
        if (indexFile.exists() && dataFile.exists()) {
            RandomAccessFile indexIn = null;
            RandomAccessFile dataIn = null;
            int indexSegment = getUidSegmentSize(day);
            int dataSegment = getPushLogSegmentSize(day);
            //System.out.println("indexSeg[" + indexSegment + "], dataSeg[" + dataSegment + "]");
            try {
                indexIn = new RandomAccessFile(index, "r");
                dataIn = new RandomAccessFile(data, "r");

                indexIn.seek(indexSegment * userId);
                long offset = 0;
                if (indexSegment == 4) {
                    offset = indexIn.readInt();
                } else {
                    offset = indexIn.readLong();
                }
                indexIn.close();

                dataIn.seek(offset * dataSegment);
                byte[] bytes = new byte[dataSegment];
                dataIn.read(bytes);
                list = decode(bytes);
                dataIn.close();
            } catch (IOException e) {
                logger.error(e);
                if (null != indexIn) {
                    indexIn.close();
                }
                if (null != dataIn) {
                    dataIn.close();
                }
            }
        }
        return list;
    }

//    public static void main(String[] args) throws IOException {
//        if (args.length < 4) {
//            System.out.println("usage: <logBaseDir> <platform> <day> <uid>");
//            return;
//        }
//        String logBaseDir = args[0];
//        String platform = args[1];
//        String day = args[2];
//        int uid = Integer.parseInt(args[3]);
//
//        System.out.println(6);
//        List<PushLog> list = getLog(logBaseDir, platform, day, uid);
//        System.out.println(GsonFactory.getPrettyGson().toJson(list));
//
//    }
//    else if (len == 30) {
//        int channelId = Integer.valueOf(ByteUtil.byte2HexStr(bytes, index + 26 + 1, index + 30), 16);
//        if (channelId < 10000 && bytes[index + 26] == 's') {
//            channel = "sc" + channelId;
//        } else {
//            channel = (char) bytes[index + 26] + String.valueOf(channelId);
//        }
//    }

    public static byte[] encodeChannel(String channel) {
        channel = channel.replaceAll("sc", "c");
        int tmp = 0;
        try {
            tmp = Integer.parseInt(channel.substring(1));
        } catch (Exception e) {
            tmp = 0;
        }
        byte[] bytes = ByteUtil.intToByteArray(tmp);
        bytes[0] = (byte)channel.charAt(0);
        return bytes;
    }

    public static class LogItem {
        private long pushTime;
        private long uid;
        private String docId;
        private String channel;
        private int pushType;
        public LogItem(long pushTime, long uid, String docId, String channel, int pushType) {
            this.pushTime = pushTime;
            this.uid = uid;
            this.channel = channel;
            this.docId = docId;
            this.pushType = pushType;
        }
    }

    public static byte[] encodeLogItem(LogItem logItem) {
        byte[] bytes = null;
        int length = 0;
        if (StringUtils.isEmpty(logItem.channel)) {
            bytes = new byte[26];
            length = 26;
        }
        else {
            bytes = new byte[30];
            length = 30;
        }
        int index = 0;
        bytes[index++] = (byte)length;
        byte[] iBytes = ByteUtil.intToByteArray(logItem.pushType);
        System.arraycopy(iBytes, 3, bytes, index, 1);
        index += 1;
        iBytes = ByteUtil.intToByteArray((int)(logItem.pushTime/1000));
        System.arraycopy(iBytes, 0, bytes, index, 4);
        index += 4;
        iBytes = ByteUtil.intToByteArray((int)logItem.uid);
        System.arraycopy(iBytes, 0, bytes, index, 4);
        index += 4;
        //System.out.println(new String(encodeDocId(logItem.docId)));
        iBytes = encodeDocId(logItem.docId);
        System.arraycopy(iBytes, 0, bytes, index, DOC_ID_DATA_LENGTH);
        index += DOC_ID_DATA_LENGTH;
        if (StringUtils.isNotEmpty(logItem.channel)) {
            iBytes = encodeChannel(logItem.channel);
            System.arraycopy(iBytes, 0, bytes, index, 4);
            index += 4;
        }
        return bytes;
    }

    public static void main(String[] args) {
        //1438082292,  1, "0A7oktXa", "u539", 2
        //long pushTime, long uid, String docId, String channel, int pushType
        PushLog.LogItem item = new PushLog.LogItem(
                1438082292297L,
                1L,
                "0A7oktXa",
                "u539",
                2

        );
        byte[] arr = PushLog.encodeLogItem(item);
        for (int i = 0; i < arr.length; i ++) {
            System.out.println(i + " " + (int)arr[i]);
        }
    }
}
