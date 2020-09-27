package com.byoskill.speedrester.openapi;

import com.byoskill.speedrester.execution.ServerConfiguration;
import com.byoskill.speedrester.model.HttpMethodEnum;
import com.byoskill.speedrester.model.HttpStatusCode;
import com.byoskill.speedrester.model.RestScenario;
import com.byoskill.speedrester.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Slf4j
public class OpenAPITestImporter {

    public static final String                       DEFAULT_TAG               = "default";
    public static final String                       TEST_DEFAULT_FOLDER       = "tests";
    public static final String                       ENTITIES                  = "entities";
    public static final String                       SERVER_CONFIGURATION_JSON = "server.configuration.json";
    private final       Map<String, JsonNode>        resourcePaths             = new HashMap<>();
    private final       Map<String, File>            tagFolder                 = new HashMap<>();
    private final       Map<String, RestTypeMapping> restTypeMapping           = new LinkedHashMap<>();
    private final       JsonGenerator                jsonGenerator;
    private             JsonNode                     openAPI;
    private             File                         outputFolder;
    private             ServerConfiguration          serverConfiguration;
    private             File                         entityFolder;
    private             File                         testFolder;
    private             boolean                      overrideEntities;
    private             boolean                      overrideTests             = true;

    public OpenAPITestImporter() {
        this.jsonGenerator = new JsonGenerator();
    }

    /**
     * Provices a customized JSON Generator if you want to produce better JSON payloads;
     *
     * @param jsonGenerator the JSON generator;
     */
    public OpenAPITestImporter(final JsonGenerator jsonGenerator) {
        this.jsonGenerator = jsonGenerator;
    }

    public static Optional<JsonNode> findFirstNode(final JsonNode field, final String... potentialFields) {
        return Arrays.stream(potentialFields).map(field::get).filter(Objects::nonNull).findFirst();
    }

    public static Optional<String> findFirstNodeName(final JsonNode field, final String... potentialFields) {
        return Arrays.stream(potentialFields).filter(fName -> field.get(fName) != null).findFirst();
    }

    private static String buildTestFileName(final String operationId) {
        return operationId + ".rest.json";
    }

    public void setOverrideEntities(final boolean overrideEntities) {
        this.overrideEntities = overrideEntities;
    }

    public void setOverrideTests(final boolean overrideTests) {
        this.overrideTests = overrideTests;
    }

    public void generateTests(final URL openAPIUrl, final File outputFolder) throws IOException {

        this.openAPI             = this.fetchOpenAPI(openAPIUrl);
        this.outputFolder        = outputFolder;
        this.serverConfiguration = this.initializeServerConfiguration();
        this.createEntityFolder(outputFolder);
        this.fetchResourcePaths();

        this.prepareTagFolder();
        this.creatingTagFolders();

        this.importTypes();
        this.generateEntities();

        this.buildingTestForPathResource();


        this.saveConfiguration();
    }

    private void createEntityFolder(final File outputFolder) {
        this.entityFolder = new File(outputFolder, ENTITIES);
        this.entityFolder.mkdirs();
    }

    private void generateEntities() {
        this.restTypeMapping.forEach((entityName, definition) -> {

            final File     entityFileName = new File(this.entityFolder, entityName + ".json");
            final JsonNode payload        = this.jsonGenerator.generate(this.restTypeMapping, definition);
            try {
                if (entityFileName.exists() && this.overrideEntities) return;
                JsonUtils.INSTANCE.writeJson(entityFileName, payload);
            } catch (final IOException e) {
                log.error("Cannot write the entity {} in {}", entityName, entityFileName, e);
            }


        });
    }

    private void importTypes() {
        final JsonNode components = this.openAPI.get("components");
        if (components == null || components.isMissingNode()) {
            log.warn("No types to import");
            return;
        }
        final JsonNode schemas = components.get("schemas");
        if (schemas == null || schemas.isMissingNode()) {
            log.warn("No types to import, no schemas");
            return;
        }
        final Iterator<String> stringIterator = schemas.fieldNames();
        while (stringIterator.hasNext()) {
            final String typeName = stringIterator.next();

            final JsonNode typeDescription = schemas.get(typeName);
            final JsonNode properties      = typeDescription.get("properties");

            final RestTypeMapping restTypeMapping = new RestTypeMapping();
            restTypeMapping.setTypeName(typeName);

            properties.fields().forEachRemaining(
                    (entry -> {
                        restTypeMapping.addField(this.parseProperty(entry));
                    })
            );


            this.restTypeMapping.put(typeName, restTypeMapping);
        }
        log.info("Imported {} types", this.restTypeMapping.size());
    }

    private RestTypeField parseProperty(final Map.Entry<String, JsonNode> entry) {
        final JsonNode      value         = entry.getValue();
        final RestTypeField restTypeField = new RestTypeField();
        final JsonNode      typeNode      = value.path("type");
        final String        type          = typeNode.asText();
        restTypeField.setName(entry.getKey());

        final JsonNode ref = value.get("$ref");
        if (ref != null) {
            restTypeField.setReference(new RefName(ref.textValue()));
        } else {
            this.parseAndSetType(value, restTypeField, type);
        }

        return restTypeField;
    }

    private void parseAndSetType(final JsonNode value, final RestTypeField restTypeField, final String type) {
        switch (type){
            case "string":
                final String format = value.path("format").asText(type);
                if (format == null) {
                    restTypeField.setBasicType(type);
                } else {
                    restTypeField.setBasicType(format);
                }
                break;
            case "integer":
            case "boolean":
                restTypeField.setBasicType(type);
                break;
            case "array":
                String refText = value.path("items").path("$ref").asText("object");
                if (refText.equals("object")) {
                    refText = value.path("items").path("type").asText();
                    this.parseAndSetType(value, restTypeField, refText);
                } else {
                    restTypeField.setReference(new RefName(refText));
                }
                restTypeField.setArray();
                break;
            default:
                log.error("Unsupported type {}", type);
                restTypeField.setBasicType(type);
                break;
        }
    }

    private void saveConfiguration() throws IOException {
        JsonUtils.INSTANCE.writeValue(new File(this.outputFolder, SERVER_CONFIGURATION_JSON), this.serverConfiguration);
    }

    private JsonNode fetchOpenAPI(final URL openAPIUrl) throws IOException {
        log.info("Fetching open API from", openAPIUrl);
        final JsonNode jsonNode = JsonUtils.INSTANCE.readURL(openAPIUrl);
        final JsonNode openapi  = jsonNode.get("openapi");
        if (openapi == null) throw new IllegalArgumentException("This is not a valid open api document");
        final String openAPIValue = openapi.asText();
        if (!(!openAPIValue.isEmpty() && openAPIValue.charAt(0) == '3')) log.warn("Open API 2.0 have not been tested.s");
        return jsonNode;
    }

    private ServerConfiguration initializeServerConfiguration() {
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        return serverConfiguration;
    }

    private void fetchResourcePaths() {
        final JsonNode paths = this.openAPI.findValue("paths");

        final Iterator<String> fieldIterator = paths.fieldNames();
        while (fieldIterator.hasNext()) {
            final String field = fieldIterator.next();
            this.resourcePaths.put(field, paths.get(field));
        }


        log.info("Found {} resource paths", this.resourcePaths.size());
    }

    private void prepareTagFolder() {
        this.testFolder = new File(this.outputFolder, TEST_DEFAULT_FOLDER);
        this.testFolder.mkdirs();
        this.tagFolder.put(DEFAULT_TAG, this.testFolder);

        this.resourcePaths.forEach((path, spec) -> {
            final Optional<JsonNode> node = this.getResourcePathMethod(spec);
            if (node.isPresent()) {
                final var    firstTagResourceName = this.getFirstTagOf(node.get());
                final String strippedTag          = StringUtils.remove(StringUtils.remove(firstTagResourceName, '/'), '\\');
                final String folderName           = CaseUtils.toCamelCase(strippedTag, true, '_').replaceAll("\\(deprecated\\)", "deprecated_");
                this.tagFolder.put(firstTagResourceName, new File(this.testFolder, folderName));
            }
        });
    }

    private void creatingTagFolders() {
        this.tagFolder.forEach((k, tagFolder) -> {
            if (!tagFolder.exists() && !tagFolder.mkdirs()) {
                log.error("Cannot create the tag folder {} for {}", tagFolder, k);
            } else {
                log.info("Tag folder {} for {} created", tagFolder, k);
            }
        });
    }

    private void buildingTestForPathResource() {
        this.resourcePaths.forEach((path, spec) -> {
            final RestScenario       restScenario       = new RestScenario();
            final Optional<JsonNode> resourcePathMethod = this.getResourcePathMethod(spec);
            final HttpMethodEnum     methodName         = this.getMethodName(spec);
            final JsonNode           endpointSpec       = resourcePathMethod.get();
            final String             tagName            = this.getFirstTagOf(endpointSpec);
            final String             operationId        = endpointSpec.get("operationId").textValue();
            final JsonNode           parameters         = endpointSpec.get("parameters");

            final OpenAPIRestPath restPath = new OpenAPIRestPath(path);

            restScenario.setTestName(tagName + " : " + operationId);
            restScenario.setMethod(methodName);
            restScenario.setResourcePath(restPath.getPath());
            restScenario.setVariables(restPath.getParams());
            restScenario.getExpectations().setHttpStatusCode(HttpStatusCode.OK);

            this.setContentType(spec, restScenario, methodName);

            restPath.getParams().forEach((k, v) -> {
                this.serverConfiguration.getParams().put(k, v);
            });

            if (parameters != null && parameters.size() > 0) {
                parameters.elements().forEachRemaining((node) -> {
                    final String variableName             = node.get("name").textValue();
                    final String variableTypeExampleValue = this.convertParamType(node);
                    final String restParamType            = node.get("in").textValue();
                    if (restParamType.equals("query")) {
                        restScenario.getQueryParams().put(variableName, null);
                        this.serverConfiguration.putParam(variableName, variableTypeExampleValue);
                    } else {
                        restScenario.getVariables().put(variableName, null);
                        this.serverConfiguration.putParam(variableName, variableTypeExampleValue);
                    }
                });
            }

            final JsonNode requestBody = endpointSpec.path("requestBody");
            if (!requestBody.isMissingNode()) {
                final JsonNode content = requestBody.get("content");
                if (content != null) {
                    final JsonNode body        = content.fields().next().getValue();
                    final JsonNode refNameNode = body.path("schema").path("$ref");
                    if (!refNameNode.isMissingNode()) {
                        final RefName refName  = new RefName(refNameNode.asText());
                        final String  typeName = refName.getTypeName();
                        restScenario.getPayload().setFilePath(ENTITIES + "/" + typeName + ".json");
                    }
                }
            }


            try {
                this.writeTestScenario(restScenario, tagName, operationId);
            } catch (final IOException e) {
                log.error("Cannot write the test scenario", e);
            }


        });
    }

    private String convertParamType(final JsonNode node) {
        final String type = node.get("schema").get("type").textValue();
        switch (type){
            case "string":
                return "exampleString";
            case "integer":
                return String.valueOf(1);
            case "boolean":
                return "false";
            default:
                return type;
        }
    }

    private void setContentType(final JsonNode spec, final RestScenario restScenario, final HttpMethodEnum methodName) {
        final JsonNode contentNode = spec.at(JsonPointer.compile("/" + methodName.name().toLowerCase() + "/requestBody/content"));

        if (!contentNode.isMissingNode()) {
            final String content = contentNode.fields().next().getKey();
            restScenario.setContentType(content);
        }
        final JsonNode contentNode2 = spec.at(JsonPointer.compile("/" + methodName.name().toLowerCase() + "/responses/default/content"));
        if (!contentNode2.isMissingNode()) {
            final String content = contentNode2.fields().next().getKey();
            restScenario.setContentType(content);
        }
    }

    private HttpMethodEnum getMethodName(final JsonNode resourcePath) {
        return HttpMethodEnum.valueOf(findFirstNodeName(resourcePath, this.getCodeNames()).orElseThrow().toUpperCase());
    }

    private Optional<JsonNode> getResourcePathMethod(final JsonNode rpath) {
        final String[] codeNames = this.getCodeNames();
        return findFirstNode(rpath, codeNames);
    }
    
    private String[] getCodeNames() {
        return Arrays.stream(HttpMethodEnum.values())
                .map(code -> code.name().toLowerCase())
                .toArray(String[]::new);
    }

    private String getFirstTagOf(final JsonNode methodSpec) {
        final JsonNode tags = methodSpec.get("tags");
        if (tags != null && tags.size() > 0) {
            return tags.get(0).asText();
        }
        return DEFAULT_TAG;
    }

    private void writeTestScenario(final RestScenario restScenario, final String tagName, final String operationId) throws IOException {

        final File outputFile = this.computeTestFileLocation(tagName, operationId);
        if (outputFile.exists() && this.overrideTests) return;
        JsonUtils.INSTANCE.writeValue(outputFile, restScenario);
    }

    private File computeTestFileLocation(final String tagName, final String operationId) {
        return new File(this.tagFolder.get(tagName), buildTestFileName(operationId));
    }

}
