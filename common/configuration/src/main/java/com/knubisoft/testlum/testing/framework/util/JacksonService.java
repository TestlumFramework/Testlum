package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public final class JacksonService {

    private final JsonMapper mapper = buildObjectMapper();
    private final JsonMapper mapperFieldVisibility = createObjectMapperWithFieldVisibility();
    private final JsonMapper deepCopy = createObjectMapperForDeepCopy();

    public <T> T readValue(final String content, final Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public <T> T readValue(final byte[] content, final Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeValueAsString(final Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeValueAsStringWithDefaultPrettyPrinter(final Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeAsStringFieldVisibility(final Object value) {
        try {
            return mapperFieldVisibility.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public <T> T readCopiedValue(final String content, final Class<T> valueType) {
        try {
            JavaType javaType = deepCopy.getTypeFactory().constructType(valueType);
            return deepCopy.readValue(content, javaType);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public String writeValueToCopiedString(final Object value) {
        try {
            return deepCopy.writeValueAsString(value);
        } catch (JacksonException e) {
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

    private JsonMapper buildObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private JsonMapper createObjectMapperWithFieldVisibility() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(inclusion ->
                        inclusion.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .changeDefaultVisibility(vc -> vc
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
                .build();
    }

    private JsonMapper createObjectMapperForDeepCopy() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.scenario.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.global_config.")
                .build();

        return JsonMapper.builder()
                .activateDefaultTyping(ptv, DefaultTyping.JAVA_LANG_OBJECT)
                .activateDefaultTyping(ptv, DefaultTyping.OBJECT_AND_NON_CONCRETE)
                .activateDefaultTyping(ptv, DefaultTyping.NON_CONCRETE_AND_ARRAYS)
                .build();
    }
}
