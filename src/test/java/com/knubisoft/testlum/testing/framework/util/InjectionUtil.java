package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
//        JavaType javaType = JacksonMapperUtil.getTypeFactory().constructType((Class<T>) t.getClass());
//        return JacksonMapperUtil.readCopiedValue(injected, javaType);
    }
}
