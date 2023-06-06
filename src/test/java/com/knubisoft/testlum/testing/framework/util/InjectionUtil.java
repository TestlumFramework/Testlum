package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
        JavaType javaType = TypeFactory.defaultInstance().constructType(t.getClass());
        return JacksonMapperUtil.readValue(injected, javaType);
    }

}
