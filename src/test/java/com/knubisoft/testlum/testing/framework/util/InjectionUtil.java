package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
        try {
            return JacksonMapperUtil.readValue(injected, (Class<T>) t.getClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
