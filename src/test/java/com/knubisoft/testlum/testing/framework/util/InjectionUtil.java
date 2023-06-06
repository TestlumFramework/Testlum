package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    private static final ObjectMapper COPY_MAPPER = createObjectMapperForDeepCopy();

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = COPY_MAPPER.writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
//        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
        JavaType javaType = COPY_MAPPER.getTypeFactory().constructType((Class<T>) t.getClass());
        return COPY_MAPPER.readValue(injected, javaType);

    }

    private ObjectMapper createObjectMapperForDeepCopy() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
//                .allowIfSubTypeIsArray()
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.util.")
//                .allowIfBaseType("java.util.List")
                .allowIfSubType("com.knubisoft.testlum.testing.model.scenario.")
//                .allowIfBaseType("com.knubisoft.testlum.testing.model.scenario.")
                .build();

        return JsonMapper.builder()
//                .polymorphicTypeValidator(ptv)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS )
//                .deactivateDefaultTyping()
//                .findAndAddModules()
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
