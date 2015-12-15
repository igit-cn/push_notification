package com.yidian.push.generator.cache;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.Platform;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Created by tianyuzhi on 15/7/9.
 */
@Log4j
public class CacheUtil {

    private static Set<String> LOCAL_CHANNELS = null;

    public static Map<Long, String> getUserIdChannelMapping(String table, List<String> channels) {
        if (null == channels || channels.size() == 0) {
            return new HashMap<>();
        }
        Map<Long, String> result = new HashMap<>();
        try {
            GeneratorConfig config = Config.getInstance().getGeneratorConfig();
            String localChannelMappingFile = config.getLocalChannelMappingFile();
            Set<String> localChannelSet = getLocalChannels(localChannelMappingFile);
            for (String channel : channels) {
                if (StringUtils.isEmpty(channel)) {
                    continue;
                }
                int index = config.getAutoRecommendCacheIndex();
                if (localChannelSet.contains(channel)) {
                    index = config.getAutoLocalCacheIndex();
                }
                List<List<Long>> userIdList = getCacheUsersWithIndex(table, channel, index);
                for (List<Long> userIds : userIdList) {
                    for (Long uid : userIds) {
                        result.put(uid, channel);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<List<Long>> getCacheUsersWithIndex(String table, String channel, int index) throws IOException {
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        int maxIndex = config.getMaxCacheIndex();
        List<List<Long>> result = new ArrayList<>(maxIndex);
        Platform platform = Platform.tableToPlatform(table);

        for (int i = 0; i < maxIndex; i++) {
            int tmp = (int) Math.pow(2.0, i * 1.0);
            if ((index & tmp) != tmp) {
                continue;
            }
            String path = new StringBuilder(config.getCacheBasePath()).append('/').append(platform.getName()).append(".").append(i).append('/').append(channel).toString();
            log.info("get users from file :" + path);
            result.add(getUserSInFile(path));
        }
        //
        return result;
    }

    public static List<Long> getLocalChannelUsers(String channel) throws IOException {
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        String channelPath = new StringBuilder(config.getLocalChannelCachePath()).append('/').append(channel).toString();
        return getUserSInFile(channelPath);
    }


    public static Set<String> getLocalChannels() {
        GeneratorConfig config = null;
        try {
            config = Config.getInstance().getGeneratorConfig();
            String localChannelMappingFile = config.getLocalChannelMappingFile();
            return getLocalChannels(localChannelMappingFile);

        } catch (IOException e) {
            log.error("could not get local channels " + ExceptionUtils.getFullStackTrace(e));
        }
        return new HashSet<>();
    }

    public static Set<String> getLocalChannels(String cityChannelMappingFile) {
        if (null != LOCAL_CHANNELS && LOCAL_CHANNELS.size() > 0) {
            return LOCAL_CHANNELS;
        }
        Set<String> localChannels = new HashSet<>(300);
        try {
            BufferedReader br = new BufferedReader(new FileReader(cityChannelMappingFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(" ");
                if (arr.length >= 2) {
                    localChannels.add(arr[1]);
                }
            }
        } catch (IOException e) {
            log.error("could not get the local channels with exception : " + ExceptionUtils.getFullStackTrace(e));
        }
        return localChannels;
    }

    public static List<Long> getUserSInFile(String file) {
        // TODO : currently the user id is saved as int, future it should be long
        FileChannel fileChannel = null;
        List<Long> result = new ArrayList<>();
        // List<Integer> tmpList = null;
        try {
            long startTime = System.currentTimeMillis();
            fileChannel = new FileInputStream(file).getChannel();
            long fileSze = fileChannel.size();
            int byteBufferSize = (int)fileSze;
            if (byteBufferSize == 0) {
                byteBufferSize = 1;
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(byteBufferSize);
            byteBuffer.clear();
            int[] arr = new int[(int) fileSze / 4];
            result = new ArrayList<>((int) fileSze / 4);
            //     tmpList = new ArrayList<>((int) fileSze / 4);

            long len = 0;
            int offset = 0;
            while ((len = fileChannel.read(byteBuffer)) != -1) {
                byteBuffer.flip();
                byteBuffer.asIntBuffer().get(arr, offset, (int) len / 4);
                offset += (int) len / 4;
                byteBuffer.clear();
            }
//           System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);
//           for (int i : arr) {
//               tmpList.add(i);
//           }
//           System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);

            // the bellow operation is time consuming.
            for (int i : arr) {
                result.add((long) i);
            }
            // System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fileChannel) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    log.error("get users from file " + file + " with exception " + ExceptionUtils.getFullStackTrace(e));
                }

            }
        }
        log.info("file : " + file + " , has users: " + result.size());
        return result;
    }

    public static List<Long> getUserSInFile2(String file) {
        // TODO : currently the user id is saved as int, future it should be long
        DataInputStream in = null;
        List<Long> result = null;
        try {
            in = new DataInputStream(new FileInputStream(file));
            long fileSze = new FileInputStream(file).getChannel().size();
            result = new ArrayList<>((int) fileSze / 4);
            while (true) {
                int i = in.readInt();
                result.add((long) i);
            }

        } catch (EOFException e) {
            //ignore
        } catch (IOException e) {
            log.error("read file " + file + " with exception: " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("get users from file " + file + " with exception " + ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
        return result;
    }

    public static List<Long> getInactiveUsers(){
        List<Long> users = null;
        try {
            GeneratorConfig config = Config.getInstance().getGeneratorConfig();


        } catch (IOException e) {
            log.error("could not get the inactive users: " + ExceptionUtils.getFullStackTrace(e));
        }
        return users;
    }
}
