package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    @SneakyThrows
//    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final TypeReference<T> ref, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.instance().writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
//        JsonNode jsonNode = JacksonMapperUtil.instance().readTree(injected);
//        Class<T> aClass = (Class<T>) t.getClass();
//        JavaType javaType = JacksonMapperUtil.instance().getTypeFactory().constructParametricType(aClass, aClass);
//        JavaType javaType = JacksonMapperUtil.instance().constructType(clazz);
        return JacksonMapperUtil.readValue(injected, ref);
//        return JacksonMapperUtil.instance().readValue(injected, javaType);
//        return JacksonMapperUtil.instance().convertValue(jsonNode, javaType);
    }

}
