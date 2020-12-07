package com.byoskill.speedrester.model;

import com.byoskill.speedrester.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RestScenario extends RestCall {
    @JsonIgnore
    private String              fileName;
    private boolean             disabled;
    private String              testName;
    private List<RestCallStep>  sequence;
    private RestExpectations    expectations = new RestExpectations();
    private Map<String, String> variables    = new HashMap<>();


    public void save(final File outputFile) throws IOException {
        JsonUtils.INSTANCE.writeValue(outputFile, this);
    }

    public boolean hasTestName() {
        return !this.testName.isBlank();
    }


    @Data
    public static class RestScenarioOptions {
        private int timeout = 5000;
    }

    @Data
    public static class RestScenarioPayload {
        private JsonNode jsonPayload;
        private String   rawPayload;
        private String   filePath;


        public String convertAsString() throws JsonProcessingException {
            if (this.rawPayload != null && !this.rawPayload.isBlank()) return this.rawPayload;
            if (this.jsonPayload != null) {
                return JsonUtils.INSTANCE.toString(this.jsonPayload);
            }
            return null;
        }
    }
}
