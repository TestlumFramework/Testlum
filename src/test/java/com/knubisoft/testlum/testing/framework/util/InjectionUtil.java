package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    @SneakyThrows
//    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.instance().writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
        JsonNode jsonNode = JacksonMapperUtil.instance().readTree(injected);
//        Class<T> aClass = (Class<T>) t.getClass();
//        JavaType javaType = JacksonMapperUtil.instance().getTypeFactory().constructParametricType(aClass, aClass);
        JavaType javaType = JacksonMapperUtil.instance().constructType(t.getClass());
//        return JacksonMapperUtil.readValue(injected, (Class<T>) t.getClass());
//        return JacksonMapperUtil.instance().readValue(injected, javaType);
        return JacksonMapperUtil.instance().convertValue(jsonNode, javaType);
    }

}
