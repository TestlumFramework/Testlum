package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class JacksonService {

    private final ObjectMapper mapper = buildObjectMapper();
    private final ObjectMapper mapperFieldVisibility = createObjectMapperWithFieldVisibility();
    private final ObjectMapper deepCopy = createObjectMapperForDeepCopy();

    public <T> T readValue(final String content, final Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public <T> T readValue(final byte[] content, final Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeValueAsString(final Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeValueAsStringWithDefaultPrettyPrinter(final Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeAsStringFieldVisibility(final Object value) {
        try {
            return mapperFieldVisibility.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public <T> T readCopiedValue(final String content, final Class<T> valueType) {
        try {
            JavaType javaType = deepCopy.getTypeFactory().constructType(valueType);
            return deepCopy.readValue(content, javaType);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeValueToCopiedString(final Object value) {
        try {
            return deepCopy.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
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
