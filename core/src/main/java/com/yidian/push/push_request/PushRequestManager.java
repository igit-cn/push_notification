package com.yidian.push.push_request;

import com.yidian.push.config.Config;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-1-12.
 */
public class PushRequestManager {
    private static PushRequestManager pushRequestManager = null;
    private PushRequestManager() {}

    public static PushRequestManager getInstance() {
        if (pushRequestManager == null) {
            synchronized (PushRequestManager.class) {
                if (pushRequestManager == null) {
                    pushRequestManager = new PushRequestManager();
                }
            }
        }
        return pushRequestManager;
    }


    public PushRequest getOneRequestAndMarkProcessing() {
        // TODO:
        return null;
    }

    public List<PushRequest> getRequests(PushRequestStatus pushRequestStatus) throws IOException {
        String dir = getRequestStatusDir(pushRequestStatus);
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

    public boolean markAsProcessed(PushRequest pushRequest) throws IOException {
        return changeRequestStatus(pushRequest, PushRequestStatus.PROCESSED);
    }

    public boolean markAsProcessing(PushRequest pushRequest) throws IOException {
        return changeRequestStatus(pushRequest, PushRequestStatus.PROCESSING);
    }

    public boolean markAsBad(PushRequest pushRequest) throws IOException {
        return changeRequestStatus(pushRequest, PushRequestStatus.BAD);
    }

    public boolean markAsPreparing(PushRequest pushRequest) throws IOException {
        return changeRequestStatus(pushRequest, PushRequestStatus.PREPARING);
    }

    public boolean markAsReady(PushRequest pushRequest) throws IOException {
        return changeRequestStatus(pushRequest, PushRequestStatus.READY);
    }

    public String getRequestStatusDir(PushRequestStatus pushRequestStatus) throws IOException {
        return new StringBuilder(Config.getInstance().getPushRequestBaseDir()).append('/').append(pushRequestStatus.toString()).toString();
    }

    private boolean changeRequestStatus(PushRequest pushRequest, PushRequestStatus newPushRequestStatus) throws IOException {
        String[] filePath = pushRequest.getFileName().split("/");
        String file = filePath[filePath.length-1];
        String newStatusDirPath = getRequestStatusDir(newPushRequestStatus);
        File newStatusDir = new File(newStatusDirPath);
        if (!newStatusDir.isDirectory()) {
            newStatusDir.mkdirs();
        }
        String newFile = new StringBuilder(newStatusDirPath).append('/').append(file).toString();
        FileUtils.moveFileToDirectory(new File(pushRequest.getFileName()), newStatusDir, true);
        pushRequest.setFileName(newFile);
        return true;
    }
}
