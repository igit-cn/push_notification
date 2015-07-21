package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushType;
import com.yidian.push.generator.data.ProtectMinutes;
import com.yidian.push.generator.data.Task;
import com.yidian.push.generator.request.Request;
import com.yidian.push.generator.request.RequestContent;
import com.yidian.push.generator.request.RequestManager;
import com.yidian.push.generator.request.RequestStatus;
import com.yidian.push.generator.util.OutServiceUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Log4j
public class Generator {
    public static void process() {
        try {
            List<Request> requests = RequestManager.getInstance().getRequests(RequestStatus.READY);

            for (Request request : requests) {
                long startTime = System.currentTimeMillis();
                log.info("start to process request:" + request.getFileName());
                try {
                    processOneRequest(request);
                } catch (Exception e) {
                    log.info("request failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                }
                long endTime = System.currentTimeMillis();
                log.info("end of processing request:" + request.getFileName() + ", cost time (seconds) : " + (endTime - startTime)/1000.0  );
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("could not get the request");
        }
    }



    public static Task genTask(RequestContent requestContent) throws IOException {
        Task task = null;
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        String head = requestContent.getHead();
        if (StringUtils.isNotEmpty(head) && head.length() > config.getValidMaxHeadLength()) {
            head = "";
        }
        List<String> channels = null;
        if (StringUtils.isNotEmpty(requestContent.getNewsChannel())) {
            channels = Arrays.asList(StringUtils.split(requestContent.getNewsChannel(), ","));
        }
        task = new Task.Builder()
                .setPushTitle(requestContent.getTitle())
                .setPushHead(head)
                .setPushDocId(requestContent.getDocId())
                .setPushDate(requestContent.getDate())
                .setPushChannel(channels)
                .setStartTime(config.getStartTime())
                .setEndTime(config.getEndTime()).build();
        return task;

    }

    public static void processOneRequest(Request request) throws IOException {
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        RequestManager.getInstance().markAsProcessing(request);
        RequestContent requestContent= null;
        try {
            requestContent = RequestContent.buildRequestContentFromFile(request.getFileName());
        } catch (IOException e) {
            log.error("bad request " + request.getFileName());
        }
        if (null == requestContent) {
            RequestManager.getInstance().markAsBad(request);
            return;
        }
        for (Platform platform : requestContent.getPlatform()) {
            String table = platform.getTable();
            List<String> uids = requestContent.getUserIds();

            if (StringUtils.isEmpty(table)) {continue;}
            if (uids.size() == 0) {continue;}

            Task task = genTask(requestContent);
            task.setTable(table);
            String uid = uids.get(0);
            int protectMinutes = ProtectMinutes.getProtectMinute(uid);
            task.setProtectMinutes(protectMinutes);
            String description = "";

            if ("auto".equals(uid)|| "auto_break".equals(uid)) {
                task.setAppIdInclude(config.getAPPID_YIDIAN());
                PushAuto.processTaskWithFile(task);
                description = "个性化";
            }
            else if ("all_yddk".equals(uid)) {
                task.setPushType(PushType.BREAK);
                task.setAppIdInclude(Arrays.asList("yddk"));
                PushAll.processTaskWithFile(task);
                description = "一点鼎开";
            }
            else if ("all".equals(uid)) {
                String title = requestContent.getTitle();
                PushType pushType = PushType.BREAK;
                if (title.startsWith("[早报]")) {
                    pushType = PushType.MORNING;
                    description = "早报";
                }
                else if (title.startsWith("[娱乐播报]'") || title.startsWith("[娱报]") || title.startsWith("[娱味]")) {
                    pushType = PushType.NOON;
                    description = "午报";
                }
                else if (title.startsWith("[晚报]")) {
                    pushType = PushType.EVENING;
                    description = "晚报";
                }
                else if (title.startsWith("[夜咖]")) {
                    pushType = PushType.NIGHT;
                    description = "夜读";
                }
                task.setPushType(pushType);
                task.setAppIdInclude(config.getAPPID_YIDIAN());
                //PushAll.processTask(task);
                PushAll.processTaskWithFile(task);
            }
            else if ("all_inactivity".equals(uid)) {
                task.setPushType(PushType.BREAK);
                List<Long> users = GetInactiveUsers.getInactiveUsers(config.getInactiveUserFilePath(),
                        config.getInactiveUserFilePrefix(),
                        config.getInactiveUserLookBackDays());
                pushToUsers(task, users);
                description = "不活跃用户";
            }
            else {
                task.setPushType(PushType.BREAK);
                List<Long> users = stringListToLongList(requestContent.getUserIds());
                pushToUsers(task, users);
                description = "突发事件";
            }
            log.info("push to " + task.getTotalPushUsers() + " " + table + " users");
        }
        RequestManager.getInstance().markAsProcessed(request);
    }

    private static List<Long> stringListToLongList(List<String> list) {
        if (null == list || list.size() == 0) {
            return new ArrayList<>();
        }
        List<Long> res = new ArrayList<>(list.size());
        for (String str : list) {
            long uid = -1;
            try {
                uid = Long.parseLong(str);
            } catch (Exception e) {
                uid = -1;
            }
            if (uid > 0) {
                res.add(uid);
            }
        }
        return res;
    }

    public static void pushToUsers(Task task, List<Long> uidList) throws IOException {
        PushUsers.processTask(task, uidList);
        //PushUsers.processTaskWithFile(task, uidList);
    }

    public static boolean sendNotificationToDataTeam(Task task, int pushNum, String desc) {
        if (Platform.isAndroid(task.getTable())) {
            OutServiceUtil.sendPushEventToDataTeam(pushNum, desc);
        }
        return true;
    }
}
