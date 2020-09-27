package com.byoskill.speedrester.openapi;

import com.byoskill.speedrester.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class JsonGenerator {
    // To avoid circles
    private final Set<String>                  visitedTypes = new HashSet<>();
    private       RestTypeMapping              definition;
    private       Map<String, RestTypeMapping> typeMappings;
    private       int                          recursionLevel;

    public JsonGenerator() {
    }

    public JsonNode generate(final Map<String, RestTypeMapping> typeMappings, final RestTypeMapping definition) {
        // To be fixed to have stateless context.
        this.typeMappings = typeMappings;
        this.definition   = definition;
        final JsonNode root = this.createRoot(this.definition);
        return root;
    }

    private JsonNode createRoot(final RestTypeMapping restTypeMapping) {
        Validate.notNull(restTypeMapping);
        final ObjectNode node = JsonUtils.INSTANCE.newObjectNode();
        this.recursionLevel++;
        restTypeMapping.getFields().forEach((fieldName, fieldSpec) -> {
            if (this.recursionLevel == 1) this.visitedTypes.clear();
            try {
                final Object fieldValue = this.convertField(fieldSpec);
                if (fieldValue instanceof JsonNode) {
                    node.put(fieldName, (JsonNode) fieldValue);
                } else if (fieldValue instanceof String) {
                    node.put(fieldName, (String) fieldValue);
                } else if (fieldValue instanceof Integer) {
                    node.put(fieldName, (Integer) fieldValue);
                } else if (fieldValue instanceof Double) {
                    node.put(fieldName, (Double) fieldValue);
                } else if (fieldValue instanceof Boolean) {
                    node.put(fieldName, (Boolean) fieldValue);
                } else if (fieldValue instanceof Date) {
                    node.put(fieldName, JsonUtils.INSTANCE.toString(fieldValue));
                } else {
                    if (fieldValue == null) {
                        // We ignore cycles
                    } else {
                        throw new UnsupportedOperationException("Unsupported converted type " + fieldSpec);
                    }

                }
            } catch (final JsonProcessingException e) {
                node.put(fieldName, "#error-" + fieldSpec);
            }

        });
        this.recursionLevel--;
        return node;
    }

    private Object convertField(final RestTypeField fieldSpec) throws JsonProcessingException {
        if (fieldSpec.isArray()) {
            return this.generateArray(fieldSpec);
        }
        return this.convertBaseType(fieldSpec);
    }

    private ArrayNode generateArray(final RestTypeField fieldSpec) throws JsonProcessingException {
        final ArrayNode arrayNode       = JsonUtils.INSTANCE.newArrayNode();
        final Object    convertBaseType = this.convertBaseType(fieldSpec);
        if (convertBaseType instanceof JsonNode) {
            arrayNode.add((JsonNode) convertBaseType);
        } else if (convertBaseType instanceof String) {
            arrayNode.add((String) convertBaseType);
        } else if (convertBaseType instanceof Integer) {
            arrayNode.add((Integer) convertBaseType);
        } else if (convertBaseType instanceof Double) {
            arrayNode.add((Double) convertBaseType);
        } else if (convertBaseType instanceof Date) {
            arrayNode.add(JsonUtils.INSTANCE.toString(convertBaseType));
        } else if (convertBaseType instanceof Boolean) {
            arrayNode.add((Boolean) convertBaseType);
        } else {
            if (convertBaseType == null) {
                // We ignore cycles
                return arrayNode;
            }
            throw new UnsupportedOperationException("Unsupported converted type " + convertBaseType);
        }
        return arrayNode;
    }

    private Object convertBaseType(final RestTypeField fieldSpec) {
        if (fieldSpec.hasReference()) {
            final String typeName = fieldSpec.getReference().getTypeName();
            if (this.visitedTypes.contains(typeName)) {
                log.warn("Detected cycle with the following relationships : " + this.visitedTypes);
                return null;
            }

            this.visitedTypes.add(typeName);
            final RestTypeMapping definition = this.typeMappings.get(typeName);
            Validate.isTrue(definition != null, "Type " + definition + " should exists");
            return this.createRoot(definition);
        }
        switch (fieldSpec.getBasicType()){
            case "integer":
                return ThreadLocalRandom.current().nextInt(20);
            case "string":
                return "string";
            case "boolean":
                return ThreadLocalRandom.current().nextBoolean();
            case "byte":
                return ThreadLocalRandom.current().nextInt(256);
            case "date-time":
                return new Date();
            default:
                return fieldSpec.getBasicType();
        }
    }
}
