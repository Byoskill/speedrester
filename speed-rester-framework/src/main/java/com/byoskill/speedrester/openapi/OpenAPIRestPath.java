package com.byoskill.speedrester.openapi;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
public class OpenAPIRestPath {

    private static final Pattern             PARAM_REGEXP = Pattern.compile(".*\\{([A-Za-z0-9]+)}.*");
    private final        Map<String, String> params       = new HashMap();
    private final        String              path;

    public OpenAPIRestPath(final String path) {
        final Matcher matcher = PARAM_REGEXP.matcher(path);
        if (matcher.matches()) {
            for (int i = 1, ni = matcher.groupCount(); i <= ni; ++i) {
                this.params.put(matcher.group(i), "");
            }
        }
        this.path = path.replace("{", "${");

    }
}
