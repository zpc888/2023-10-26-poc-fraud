package com.prot.erd.service;

import com.prot.poc.common.AppException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T17:27 Thursday
 */
public class AspectObjectsLoader {
   public List<Pair<String, String>> loadAspectObjects(String dir, String filename) {
       File f = new File(dir, filename);
       try {
           List<String> lines = Files.lines(f.toPath(), StandardCharsets.UTF_8).collect(Collectors.toList());
           List<Pair<String, String>> ret = new ArrayList<>(384);
           String prevAspect = null;
           for (String line: lines) {
               if (line == null || (line = line.trim()).isEmpty()) {
                   continue;        // empty line
               }
               if (line.startsWith("#") || line.startsWith("//")) {
                   continue;        // comment line
               }
               if (line.startsWith("--") || line.startsWith("==")) {
                   // change aspect
                   prevAspect = line.substring(2).trim();
               } else {
                   // object api name
                   ret.add(Pair.of(prevAspect, line));
               }
           }
           return ret;
       } catch (Exception ex) {
           throw new AppException("Fail to load apsect objects file: " + f.getAbsolutePath(), ex);
       }
   }
}
