package com.yidian.push.generator.request;

import com.yidian.push.config.Config;
import com.yidian.push.push_request.PushRequest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-3-4.
 */
public class RequestManager {
    private static RequestManager requestManager = null;
    private RequestManager() {}
    public static RequestManager getInstance() {
        if (requestManager == null) {
            synchronized (RequestManager.class) {
                if (requestManager == null) {
                    requestManager = new RequestManager();
                }
            }
        }
        return requestManager;
    }

    public List<PushRequest> getRequests(RequestStatus requestStatus) throws IOException {
        String dir = getRequestStatusDir(requestStatus);
        List<PushRequest> list = new ArrayList<>();
        File dirFile = new File(dir);
        if (dirFile.isDirectory() && dirFile.exists()) {
            File[] files = dirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    list.add(new PushRequest(file.getAbsolutePath()));
                }
            }
        }
        return list;
    }

    public boolean markAsProcessed(Request request) throws IOException {
        return changeRequestStatus(request, RequestStatus.PROCESSED);
    }

    public boolean markAsProcessing(Request request) throws IOException {
        return changeRequestStatus(request, RequestStatus.PROCESSING);
    }

    public boolean markAsBad(Request request) throws IOException {
        return changeRequestStatus(request, RequestStatus.BAD);
    }

    public String getRequestStatusDir(RequestStatus requestStatus) throws IOException {
        return new StringBuilder(Config.getInstance().getRequestBaseDir()).append('/').append(requestStatus.toString()).toString();
    }

    private boolean changeRequestStatus(Request request, RequestStatus newStatus) throws IOException {
        String[] filePath = request.getFileName().split("/");
        String file = filePath[filePath.length-1];
        String newStatusDirPath = getRequestStatusDir(newStatus);
        File newStatusDir = new File(newStatusDirPath);
        if (!newStatusDir.isDirectory()) {
            newStatusDir.mkdirs();
        }
        String newFile = new StringBuilder(newStatusDirPath).append('/').append(file).toString();
        FileUtils.moveFileToDirectory(new File(request.getFileName()), new File(newFile), true);
        request.setFileName(newFile);
        return true;
    }
}
