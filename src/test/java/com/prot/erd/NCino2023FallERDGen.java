package com.prot.erd;

import com.prot.erd.service.PlantUMLGen;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T23:25 Wednesday
 */
public class NCino2023FallERDGen {
    public static void main(String[] args) {
        PlantUMLGen plantUMLGen = new PlantUMLGen();
        plantUMLGen.setOutputFileFormatter("ncino-2023-fall-", "-erd");
        plantUMLGen.setNiceTitleFormatter("nCino 2023 Fall - ", " ERD");
        plantUMLGen.genUML("src/test/resources/erd/ncino-erds-2023-fall", "docs");
    }
}
