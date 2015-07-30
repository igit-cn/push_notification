package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.data.MessageType;
import com.yidian.push.data.PushChannel;
import com.yidian.push.push_request.PushRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.*;

/**
 * Created by tianyuzhi on 15/7/28.
 */
public class FilterToken {
    private static final int XIAOMI_TOKEN_INFO = 1;
    private static final int UMENG_TOKEN_INFO = 2;
    private static final int GETUI_TOKEN_INFO = 4;
    private static final int SUPPORT_PASS_THROUGH_UNIQUE = 1;
    private static final int NOTIFICATION = 0;
    private static final int MESSAGE = 1;
    private static final String XIAOMI_TOKEN_PREFIX = "MMPP";
    private static final String UMENG_TOKEN_PREFIX = "UMPP";
    private static final String GETUI_TOKEN_PREFIX = "GTPP";
    //TODO: Care the order for the TOKEN_INFOS
    private static final List<Integer> TOKEN_INFOS = Arrays.asList(XIAOMI_TOKEN_INFO, GETUI_TOKEN_INFO, UMENG_TOKEN_INFO);
    private static Map<String, Integer> TOKEN_PREFIX_INFO_MAPPING = new HashMap<>(TOKEN_INFOS.size());
    private static Map<Integer, String> TOKEN_INFO_PREFIX_MAPPING = new HashMap<>(TOKEN_INFOS.size());
    private static Map<Integer, PushChannel> TOKEN_INFO_PUSH_CHANNEL_MAPPING = new HashMap<>();

    static {
        TOKEN_PREFIX_INFO_MAPPING.put(XIAOMI_TOKEN_PREFIX, XIAOMI_TOKEN_INFO);
        TOKEN_PREFIX_INFO_MAPPING.put(UMENG_TOKEN_PREFIX, UMENG_TOKEN_INFO);
        TOKEN_PREFIX_INFO_MAPPING.put(GETUI_TOKEN_PREFIX, GETUI_TOKEN_INFO);

        for (Map.Entry<String , Integer> item : TOKEN_PREFIX_INFO_MAPPING.entrySet()) {
            TOKEN_INFO_PREFIX_MAPPING.put(item.getValue(), item.getKey());
        }

        TOKEN_INFO_PUSH_CHANNEL_MAPPING.put(XIAOMI_TOKEN_INFO, PushChannel.XIAOMI);
        TOKEN_INFO_PUSH_CHANNEL_MAPPING.put(UMENG_TOKEN_INFO, PushChannel.UMENG);
        TOKEN_INFO_PUSH_CHANNEL_MAPPING.put(GETUI_TOKEN_INFO, PushChannel.GETUI);
    }
    @Getter
    public static class TokenPushChannelMessageType {
        public String token;
        public PushChannel pushChannel;
        public MessageType messageType;
        public TokenPushChannelMessageType(String token, PushChannel pushChannel, MessageType messageType) {
            this.token = token;
            this.pushChannel = pushChannel;
            this.messageType = messageType;
        }
    }

    @Getter
    public static class TokenPushLevelPair {
        public String token = null;
        public int pushLevel = 0;

        public TokenPushLevelPair(String token, String pushLevel) {
            this.token = token;
            int tmp = 0;
            try {
                tmp = Integer.parseInt(pushLevel);
            } catch (Exception e) {
                tmp = 0;
            }
            this.pushLevel = tmp;
        }
    }

    @Setter
    @Getter
    public static class Level_Support_Number {
        public int level = 0;
        public int support = 0;
        public int number = 0;
    }

    public static List<TokenPushChannelMessageType> filterTokens(PushRecord pushRecord, PushChannel pushChannel) throws IOException {
        return filterTokens(pushRecord, pushChannel, true);
    }

    public static List<TokenPushChannelMessageType> filterTokens(PushRecord pushRecord, PushChannel pushChannel, boolean doFilter) throws IOException {
        List<String> tokens = pushRecord.getTokens();
        if (null == tokens || tokens.size() == 0) {
            return new ArrayList<>(0);
        }
        List<TokenPushChannelMessageType> res = new ArrayList<>(tokens.size());
        List<TokenPushLevelPair> tokenLevelList = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            String[] arr = token.split(PushRecord.TOKEN_ITEM_SEPARATOR, 2);
            if (arr.length < 2) {
                return res;
            }
            tokenLevelList.add(new TokenPushLevelPair(arr[0], arr[1]));
        }
        if (!doFilter || pushChannel == PushChannel.IOS) {
            for (TokenPushLevelPair item : tokenLevelList) {
                res.add(new TokenPushChannelMessageType(item.token, pushChannel, MessageType.NOTIFICATION));
            }
            return res;
        }

        Map<String, Level_Support_Number> macInfo = new HashMap<>(tokenLevelList.size());

        for (TokenPushLevelPair item : tokenLevelList) {
            String tokenInfo = item.token.substring(0, 4);
            String mac = item.token.substring(4);
            Level_Support_Number level_support_number = null;
            if (!macInfo.containsKey(mac)) {
                level_support_number =  new Level_Support_Number();
            }
            else {
                level_support_number = macInfo.get(mac);
            }
            level_support_number.level |= item.pushLevel;
            int support = TOKEN_PREFIX_INFO_MAPPING.containsKey(tokenInfo) ? TOKEN_PREFIX_INFO_MAPPING.get(tokenInfo) : 0;
            level_support_number.support = support;
            level_support_number.number ++;
        }

        if (Config.getInstance().getProcessorConfig().isXiaomi(pushRecord.getAppId())) {
            MessageType messageType = MessageType.NOTIFICATION;
            for (String mac : macInfo.keySet()) {
                Level_Support_Number level_support_number = macInfo.get(mac);
                if ((level_support_number.support & XIAOMI_TOKEN_INFO) != 0) {
                    res.add(new TokenPushChannelMessageType(XIAOMI_TOKEN_PREFIX + mac, PushChannel.XIAOMI, messageType));
                }
            }
        }
        else {
            for (String mac : macInfo.keySet()) {
                Level_Support_Number level_support_number = macInfo.get(mac);
                if (pushChannel == PushChannel.XIAOMI && (level_support_number.support & XIAOMI_TOKEN_INFO) != 0) {
                    MessageType messageType = MessageType.NOTIFICATION;
                    res.add(new TokenPushChannelMessageType(XIAOMI_TOKEN_PREFIX + mac, PushChannel.XIAOMI, messageType));
                }
                else if ((level_support_number.level & SUPPORT_PASS_THROUGH_UNIQUE) > 0
                        && level_support_number.number > 1) {
                    MessageType messageType = MessageType.MESSAGE;
                    for (int tokenInfo : TOKEN_INFOS) {
                        if ((level_support_number.support & tokenInfo) != 0) {
                            res.add(new TokenPushChannelMessageType(TOKEN_INFO_PREFIX_MAPPING.get(tokenInfo)+mac, TOKEN_INFO_PUSH_CHANNEL_MAPPING.get(tokenInfo), messageType));
                        }
                    }
                }
                else {
                    MessageType messageType = MessageType.NOTIFICATION;
                    /* if only has umeng token for one mac,
                     use notification instead of message for the former
                     has the higher reach rate*/
                    for (int tokenInfo : TOKEN_INFOS) {
                        if ((level_support_number.support & tokenInfo) != 0) {
                            res.add(new TokenPushChannelMessageType(TOKEN_INFO_PREFIX_MAPPING.get(tokenInfo)+mac, TOKEN_INFO_PUSH_CHANNEL_MAPPING.get(tokenInfo), messageType));
                        }
                    }
                }


            }
        }
        return res;
    }
}
