package com.yidian.push.utils;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-4-27.
 */
public class FileUtil {
    public static String getAppId(String fileName) {
        if (fileName == null) {
            return "";
        }
        String[] arr = fileName.split("/");
        String appId = arr[arr.length-1].split("\\.")[0];
        return appId;
    }
    public static List<String> getFiles(String pattern, String base) throws IOException {
        List<String> res = new ArrayList<>();
        File dir = new File(base);
        FileFilter fileFilter = new WildcardFileFilter(pattern);
        File[] files = dir.listFiles(fileFilter);
        if (files != null) {
            for (File file : files) {
                res.add(file.getAbsolutePath());
            }
        }
        return res;
    }


    public static List<String> getFiles2(String pattern, String base) throws IOException {
        final List<String> res = new ArrayList<>();
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        Files.walkFileTree(Paths.get(base), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(file)) {
                    res.add(file.toAbsolutePath().toString());
                    System.out.println("MATCHES>>"+file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return res;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(GsonFactory.getNonPrettyGson().toJson(getFiles("*.txt", "/Users/yidianadmin")));
        System.out.println(GsonFactory.getNonPrettyGson().toJson(getFiles2("*.txt", "/Users/yidianadmin")));
     //   System.out.println(GsonFactory.getNonPrettyGson().toJson(getFiles2("/Users/yidianadmin/*.txt")));
    }
}
