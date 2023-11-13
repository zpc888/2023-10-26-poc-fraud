package com.prot.poc.esign.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-09T23:30 Thursday
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__type")
@JsonSubTypes({
        @JsonSubTypes.Type(name="ViaXY", value= TouchLocation.ViaXY.class),
        @JsonSubTypes.Type(name="ViaAnchor", value= TouchLocation.ViaAnchor.class)
})
sealed public interface TouchLocation permits TouchLocation.ViaXY, TouchLocation.ViaAnchor {

    record ViaXY(int pageNumber, int xPosition, int yPosition) implements TouchLocation {
        // pageNumber is 1-base, not 0-base
        // xPosition, yPosition: 720 dpi
    }

    @RequiredArgsConstructor
    @Data
    @Accessors(chain = true)
    final class ViaAnchor implements TouchLocation {
        private final String anchorName;
        private final int offsetX;
        private final int offsetY;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private boolean anchorCaseSensitive = false;
        private String anchorUnits;         // Units could be pixels, millimeters, centimeters, or inches
    }
}