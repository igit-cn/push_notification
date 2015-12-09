package com.yidian.push.recommend_gen;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by tianyuzhi on 15/12/9.
 */
@Log4j
public class FilterUsers {

    public static List<String> getMatchFiles(String glob, final String location) throws IOException {
        final List<String> files = new ArrayList<>();
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path name = file.getFileName();
                        if (pathMatcher.matches(name) && !file.toFile().isDirectory()) {
                            files.add(file.toFile().getAbsolutePath());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
        return files;
    }

    public static Set<String> getUsers(String filterPath, int lookBackDay, boolean isFilterEnabled){
        Set<String> users = new HashSet<>();
        if (!isFilterEnabled) {
            return users;
        }
        List<String> files = new ArrayList<>();
        for (int i = 0; i < lookBackDay; i ++) {
            String day = DateTime.now().minusDays(i).toString("yyyy-MM-dd");
            String pattern = "*" + day;
            try {
                List<String> dayFiles = getMatchFiles(pattern, filterPath);
                files.addAll(dayFiles);
            } catch (IOException e) {
                log.error("get files error : " + ExceptionUtils.getFullStackTrace(e));
            }
        }
        Charset UTF_8 = StandardCharsets.UTF_8;
        for (String file : files)  {
            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader(new File(file).toPath(), UTF_8);
                String line = null;
                Set<String> fileUsers = new HashSet<>();
                while ((line = reader.readLine()) != null) {
                    fileUsers.add(line);
                }
                log.info("get " + fileUsers.size() + " users from file " + file);
                users.addAll(fileUsers);
            } catch (IOException e) {
                log.error("could not get user from file :" + file + ", with exception:" + ExceptionUtils.getFullStackTrace(e));
            }
            finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return users;
    }
}
