package com.byoskill.speedrester.openapi;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RefName {
    public static final String COMPONENTS_SCHEMAS = "#/components/schemas/";
    private final       String referenceName;
    private final       String typeName;

    public RefName(final String referenceName) {
        this.referenceName = referenceName;
        final int beginIndex = referenceName.indexOf(COMPONENTS_SCHEMAS);
        if (beginIndex == -1) {
            System.out.println(referenceName);
        }
        this.typeName = referenceName.substring(COMPONENTS_SCHEMAS.length());
    }
}
