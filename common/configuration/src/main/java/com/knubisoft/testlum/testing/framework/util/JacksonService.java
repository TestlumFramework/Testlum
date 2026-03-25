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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public final class JacksonService {

    private final ObjectMapper mapper = buildObjectMapper();
    private final ObjectMapper mapperFieldVisibility = createObjectMapperWithFieldVisibility();
    private final ObjectMapper deepCopy = createObjectMapperForDeepCopy();

    @SneakyThrows
    public <T> T readValue(final String content, final Class<T> valueType) {
        return mapper.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readValue(final byte[] content, final Class<T> valueType) {
        return mapper.readValue(content, valueType);
    }

    @SneakyThrows
    public String writeValueAsString(final Object value) {
        return mapper.writeValueAsString(value);
    }

    @SneakyThrows
    public String writeValueAsStringWithDefaultPrettyPrinter(final Object value) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    @SneakyThrows
    public String writeAsStringFieldVisibility(final Object value) {
        return mapperFieldVisibility.writeValueAsString(value);
    }

    @SneakyThrows
    public <T> T readCopiedValue(final String content, final Class<T> valueType) {
        JavaType javaType = deepCopy.getTypeFactory().constructType(valueType);
        return deepCopy.readValue(content, javaType);
    }

    @SneakyThrows
    public String writeValueToCopiedString(final Object value) {
        return deepCopy.writeValueAsString(value);
    }

    public <T> T deepCopy(final Object value, final Class<T> valueType) {
        return deepCopy.convertValue(value, valueType);
    }

    public Object toJsonObject(final String content) {
        if (StringUtils.isNotBlank(content)) {
            if (content.startsWith("{") && content.endsWith("}")) {
                return readValue(content, Object.class);
            }
            if (content.startsWith("[") && content.endsWith("]")) {
                return readValue(content, Object.class);
            }
        }
        return content;
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
                .allowIfSubType("java.util.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.scenario.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.global_config.")
                .build();

        return JsonMapper.builder()
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS)
                .build();
    }
}
