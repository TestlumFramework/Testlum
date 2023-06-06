package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        return injectObject(t, (Class<T>) t.getClass(), scenarioContext);
    }

    public <T> T injectObject(final T t, final Class<T> clazz, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
        JavaType javaType = TypeFactory.defaultInstance().constructType(clazz);
        return JacksonMapperUtil.readValue(injected, javaType);
    }
}
