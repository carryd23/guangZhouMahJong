package com.cd.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CmdClassScanner {
    private static final String SEP = File.separator;

    public List<String> getClassNames() {
        try{
            List<String> className = new ArrayList<>();
            File file = new File(Paths.get("src", SEP, "main", SEP, "java").toString());
            List<Path> paths = getJavaFiles(file);
            Path parentPath = Paths.get(file.getCanonicalPath());
            for(Path path : paths) {
                String fileName = parentPath.relativize(path).toString();
                className.add(fileName.substring(0, fileName.lastIndexOf(".")).replace(SEP, "."));
            }
            return className;
        }catch(IOException e) {
            throw new RuntimeException("classpath error");
        }
    }

    private List<Path> getJavaFiles(File file) throws IOException{
        List<Path> s = new ArrayList<>();
        File[] files = file.listFiles();
        for(File f : files) {
            if(f.isDirectory() && !f.getName().contains("core")) {
                s.addAll(getJavaFiles(f));
            }else {
                String string = f.getCanonicalPath();
                if(string.endsWith(".java")) {
                    s.add(Paths.get(string));
                }
            }
        }
        return s;
    }
}
