package com.byoskill.speedrester.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * THis class offers basic manipulations of JSon structures ( read, toString, write )
 */
public class JsonUtils {

    public static final JsonUtils    INSTANCE = new JsonUtils();
    private final       ObjectMapper objectMapper;


    public JsonUtils() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    public JsonNode readTree(final String jsonPayload) throws JsonProcessingException {
        return this.objectMapper.readTree(jsonPayload);
    }


    public String toString(final JsonNode assertion) throws JsonProcessingException {
        return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(assertion);
    }

    public void writeValue(final File outputFile, final Object value) throws IOException {
        this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, value);
    }

    public <T> T readValue(final File resource, final Class<T> expectedType) throws IOException {
        return this.objectMapper.readValue(resource, expectedType);
    }

    public String toString(final Object jsonPathValue) throws JsonProcessingException {
        return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonPathValue);
    }

    public JsonNode readURL(final URL openAPIUrl) throws IOException {
        final JsonNode jsonNode = this.objectMapper.readTree(openAPIUrl);
        return jsonNode;
    }

    public void writeJson(final File entityFileName, final JsonNode payload) throws IOException {
        this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(entityFileName, payload);
    }

    public ObjectNode newObjectNode() {
        return this.objectMapper.createObjectNode();
    }

    public ArrayNode newArrayNode() {
        return this.objectMapper.createArrayNode();
    }

    public <T> T readValue(final InputStream resourceAsStream, final Class<?> clazz) throws IOException {
        return (T) this.objectMapper.readValue(resourceAsStream, clazz);
    }
}
