package com.prot.erd.model;

import com.prot.poc.common.AppException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T20:54 Wednesday
 */
@Data
@Accessors(chain = true)
public class Relationship {
    private final String fromObjectCode;
    private final Cardinality fromObjectCardinality;
    private final String toObjectCode;
    private final Cardinality toObjectCardinality;
    private String fromObjectRole;
    private String toObjectRole;
    private String relationshipDescription;

    public Relationship(String fromObjectCode, Cardinality fromCardinality,
                        String toObjectCode, Cardinality toCardinality) {
        this.fromObjectCode = fromObjectCode;
        this.fromObjectCardinality = fromCardinality;
        this.toObjectCode = toObjectCode;
        this.toObjectCardinality = toCardinality;
    }

    public Relationship(String fromObjectCode, String fromCardinality,
                        String toObjectCode, String toCardinality) {
        this(fromObjectCode, Cardinality.fromValue(fromCardinality),
                toObjectCode, Cardinality.fromValue(toCardinality));
    }

    public enum Cardinality {
        ZERO_OR_ONE("0..1"),
        EXACTLY_ONE("1..1"),
        ZERO_OR_MANY("0..n"),
        ONE_OR_MANY("1..n");

        private String val;
        Cardinality(String value) {
            this.val = value;
        }

        public String plantumlAtToSide() {
            return switch (this) {
                case ZERO_OR_ONE -> "o|";
                case EXACTLY_ONE -> "||";
                case ZERO_OR_MANY -> "o{";
                case ONE_OR_MANY -> "|{";
            };
        }

        public String plantumlAtFromSide() {
            return switch (this) {
                case ZERO_OR_ONE -> "|o";
                case EXACTLY_ONE -> "||";
                case ZERO_OR_MANY -> "}o";
                case ONE_OR_MANY -> "}|";
            };
        }

        public static Cardinality fromValue(String value) {
            for (Cardinality t: Cardinality.values()) {
                if (t.val.equals(value)) {
                    return t;
                }
            }
            throw new AppException("No matched relationship type with [" + value + "] value");
        }
    }
}
