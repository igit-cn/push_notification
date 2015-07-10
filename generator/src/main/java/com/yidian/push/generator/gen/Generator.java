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
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
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
        task = new Task.Builder()
                .setPushTitle(requestContent.getTitle())
                .setPushHead(head)
                .setPushDocId(requestContent.getDocId())
                .setPushDate(requestContent.getDate())
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

            if ("auto".equals(uid)|| "auto_break".equals(uid)) {
                PushAuto.processTaskWithFile(task);
            }
            else if ("all_yddk".equals(uid)) {
                task.setPushType(PushType.BREAK);
                task.setAppIdInclude(Arrays.asList("yddk"));
                PushAll.processTaskWithFile(task);
            }
            else if ("all".equals(uid)) {
                String title = requestContent.getTitle();
                PushType pushType = PushType.BREAK;
                if (title.startsWith("[早报]")) {
                    pushType = PushType.MORNING;
                }
                else if (title.startsWith("[娱乐播报]'") || title.startsWith("[娱报]") || title.startsWith("[娱味]")) {
                    pushType = PushType.NOON;
                }
                else if (title.startsWith("[晚报]")) {
                    pushType = PushType.EVENING;
                }
                else if (title.startsWith("[夜咖]")) {
                    pushType = PushType.NIGHT;
                }
                task.setPushType(pushType);
                task.setAppIdInclude(config.getAPPID_YIDIAN());
                //PushAll.processTask(task);
                PushAll.processTaskWithFile(task);
            }
        }
        RequestManager.getInstance().markAsProcessed(request);
    }
}
