package com.yidian.push.recommend_gen;

/**
 * Created by tianyuzhi on 15/12/9.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class FileFinderDemo {
    public static void main(String[] args) throws IOException {
        // create a FileFinder instance with a naming pattern
        FileFinder finder = new FileFinder("*.log");

        // pass the initial directory and the finder to the file tree walker
        Files.walkFileTree(Paths.get("/tmp"), finder);

        // get the matched paths
        Collection<Path> matchedFiles = finder.getMatchedPaths();

        // print the matched paths
        for (Path path : matchedFiles) {
            System.out.println(path.getFileName());
        }
    }
}