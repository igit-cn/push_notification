package com.yidian.push.generator.data;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by tianyuzhi on 15/7/7.
 * Not Thread Safe
 */
@Getter
public class PushIndex {
    private String dataPath = null;
    private String day = null;

    public PushIndex(String dataPath, String day) {
        this.dataPath = dataPath;
        this.day = day;
    }

    public PushIndex(String dataPath) {
        if (StringUtils.isEmpty(dataPath)) {
            return;
        }
        String[] arr = dataPath.split("/");
        if (arr.length >= 1) {
            this.dataPath = dataPath;
            this.day = arr[arr.length - 1];
        }
    }

    public boolean isOlderThan(PushIndex other) {
        if (null == other || other.isEmpty() || this.isEmpty()) {
            return false;
        }
        else {
            return this.day.compareTo(other.day) < 0;
        }
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(dataPath);
    }

    private String getWorkingFile() {
        return dataPath + "/working";
    }

    public boolean isInUse() {
        String workingFile = getWorkingFile();
        return new File(workingFile).exists();
    }

    public void markAsUsing() throws IOException {
        String workingFileName = getWorkingFile();
        File workingFile = new File(workingFileName);
        if (workingFile.exists()) {
            throw new RuntimeException("the index file is already in use");
        } else {
            workingFile.createNewFile();
        }
    }

    public void markAsNoneUsing() throws IOException {
        String workingFileName = getWorkingFile();
        File workingFile = new File(workingFileName);
        if (!workingFile.exists()) {
            throw new RuntimeException("the index file is not in use");
        } else {
            FileUtils.forceDelete(workingFile);
        }
    }
}
