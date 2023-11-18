package com.prot.erd;

import com.prot.erd.service.AspectObjectsLoader;
import com.prot.erd.service.PlantUMLGen;
import com.prot.erd.service.SOQLGen;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T18:25 Thursday
 */
public class NCino2023FallSOQLGen {
    public static void main(String[] args) {
        genSOQL();
        genERDs();
    }

    private static void genERDs() {
        PlantUMLGen plantUMLGen = new PlantUMLGen();
        plantUMLGen.setOutputFileFormatter("ncino-2023-fall-", "-auto-gen");
        plantUMLGen.setNiceTitleFormatter("nCino 2023 Fall - ", " - AutoGen");
        plantUMLGen.genAspectERDAutomatically("src/test/resources/erd/ncino-erds-2023-fall/auto-discover-relationships",
                "docs/auto-discover-relationships");
    }

    private static void genSOQL() {
        AspectObjectsLoader loader = new AspectObjectsLoader();
        List<Pair<String, String>> pairs = loader.loadAspectObjects(
                "src/test/resources/erd/ncino-erds-2023-fall/auto-discover-relationships",
                "aspect-objects.txt");
        new SOQLGen().genSOQLWithAspectObjects(pairs, "/tmp/ncino-2023-fall-soql.txt");
    }
}
