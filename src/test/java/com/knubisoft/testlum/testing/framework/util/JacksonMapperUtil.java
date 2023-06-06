package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public final class JacksonMapperUtil {

    private static final ObjectMapper MAPPER = buildObjectMapper();
    private static final ObjectMapper DYNAMODB_MAPPER = createObjectMapperWithFieldVisibility();
    private static final ObjectMapper COPY_MAPPER = createObjectMapperForDeepCopy();

    @SneakyThrows
    public <T> T readValue(final String content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readValue(final File content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readCopiedValue(final String content, final Class<T> valueType) {
        JavaType javaType = COPY_MAPPER.getTypeFactory().constructType(valueType);
        return COPY_MAPPER.readValue(content, javaType);
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
    public String writeAsStringForDynamoDbOnly(final Object value) {
        return DYNAMODB_MAPPER.writeValueAsString(value);
    }

    private ObjectMapper buildObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private ObjectMapper createObjectMapperWithFieldVisibility() {
        ObjectMapper mapper = new ObjectMapper();
        VisibilityChecker<?> config = configMapper(mapper);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(config);
        return mapper;
    }

    private VisibilityChecker<?> configMapper(final ObjectMapper mapper) {
        return mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
    }

    private ObjectMapper createObjectMapperForDeepCopy() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubTypeIsArray()
                .allowIfSubType("java.util.List")
                .allowIfBaseType("java.util.List")
                .allowIfSubType("com.knubisoft.testlum.testing.model.scenario.")
                .allowIfBaseType("com.knubisoft.testlum.testing.model.scenario.")
                .build();

        return JsonMapper.builder()
                .polymorphicTypeValidator(ptv)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.WRAPPER_OBJECT)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.WRAPPER_ARRAY)
//                .deactivateDefaultTyping()
                .findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

}
