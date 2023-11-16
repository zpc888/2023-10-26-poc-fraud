package com.prot.erd.service;

import com.prot.erd.model.AspectDefinition;
import com.prot.poc.common.AppException;
import com.prot.erd.model.AllDefinition;
import com.prot.erd.model.Relationship;
import com.prot.erd.model.SObject;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T20:52 Wednesday
 */
public class FilesLoader {

    public static final String ALL_OBJECTS_DEFINITION = "all-objects.properties";

    public AllDefinition loadDir(String erdDefRootDir) {
        return loadDir(new File(erdDefRootDir));
    }

    public AllDefinition loadDir(File dir) {
        if (!dir.exists()) {
            throw new AppException("Folder: " + dir.getAbsolutePath() + " doesn't exist");
        }
        if (dir.isFile()) {
            throw new AppException("Folder: " + dir.getAbsolutePath() + " is not directory");
        }
        try {
            Map<String, SObject> sObjectMap = loadSObjectMap(dir, ALL_OBJECTS_DEFINITION);
            AllDefinition ret = new AllDefinition(sObjectMap);
            File[] files = dir.listFiles( f -> f.getName().endsWith(AspectDefinition.ASPECT_ERD_DEFINITION_SUFFIX));
            ret.setAllAspects(Arrays.stream(files).map(this::parseOneAspect).collect(Collectors.toList()));
            return ret;
        } catch (Exception ex) {
            if (ex instanceof AppException ae) {
                throw ae;
            } else {
                throw new AppException("Fail to parse folder: " + dir.getAbsolutePath(), ex);
            }
        }
    }

    private AspectDefinition parseOneAspect(File file) {
        AspectDefinition ret = new AspectDefinition();
        ret.setFileName(file.getName());
        try {
            ret.setRelationships(Files.readAllLines(file.toPath()).stream()
                    .map(this::parseRelationship)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } catch (IOException ioex) {
            throw new AppException("Fail to parse apsect file: " + file.getName(), ioex);
        }
        return ret;
    }

    private Relationship parseRelationship(String line) {
        if (line == null || (line = line.trim()).isEmpty()) {
            return null;
        }
        if (line.startsWith("#") || line.startsWith("//")) {
            return null;
        }
        String[] values = splitLine(line);
        Relationship ret = new Relationship(values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim());
        if (values.length >= 5) {
            ret.setFromObjectRole(values[4].trim());
        }
        if (values.length >= 6) {
            ret.setToObjectRole(values[5].trim());
        }
        if (values.length >= 7) {
            ret.setRelationshipDescription(values[6].trim());
        }
        return ret;
    }

    private Map<String, SObject> loadSObjectMap(File dir, String sObjectProperties) throws Exception {
        try (InputStream is = new FileInputStream(new File(dir, sObjectProperties))) {
            Properties p = new Properties();
            p.load(is);
            Map<String, SObject> ret = new HashMap<>(p.size());
            for (Object k: p.keySet()) {
                ret.put((String)k, toSObject((String)k, (String)p.get(k)));
            }
            return ret;
        }
    }

    private SObject toSObject(String k, String v) {
        String[] split = splitLine(v);
        String apiName = null;
        String displayName = null;
        String description = null;
        if (split.length >= 1) {
            apiName = split[0].trim();
        }
        if (split.length >= 2) {
            displayName = split[1].trim();
        }
        if (split.length >= 3) {
            description = split[2].trim();
        }
        return new SObject(k, apiName, displayName, description);
    }

    String[] splitLine(String line) {
        return line.split(",");
    }
}
