package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public final class JacksonMapperUtil {

    private static final ObjectMapper MAPPER = buildObjectMapper();
    private static final ObjectMapper MAPPER_TYPE = buildObjectMapper();
    private static final ObjectMapper DYNAMODB_MAPPER = createObjectMapperWithFieldVisibility();

    @SneakyThrows
    public <T> T readValue(final String content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readValue(final String content, final JavaType javaType) {
        return MAPPER.readValue(content, javaType);
    }

    @SneakyThrows
    public <T> T readValue(final File content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public String writeValueAsString(final Object value) {
        return MAPPER.writeValueAsString(value);
    }

    @SneakyThrows
    public String writeValueAsStringWithDefaultPrettyPrinter(final Object value) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    @SneakyThrows
    public static String writeAsStringForDynamoDbOnly(final Object value) {
        return DYNAMODB_MAPPER.writeValueAsString(value);
    }

    private static ObjectMapper buildObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private static ObjectMapper createObjectMapperWithFieldVisibility() {
        ObjectMapper mapper = new ObjectMapper();
        VisibilityChecker<?> config = configMapper(mapper);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(config);
        return mapper;
    }

    private static VisibilityChecker<?> configMapper(final ObjectMapper mapper) {
        return mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
    }

    public ObjectMapper instance() {
        return MAPPER_TYPE;
    }
}
