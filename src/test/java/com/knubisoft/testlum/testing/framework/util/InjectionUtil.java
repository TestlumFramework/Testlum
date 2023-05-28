package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InjectionUtil {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.instance().writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
//            JsonNode jsonNode = JacksonMapperUtil.instance().readTree(injected);
//        T o = JacksonMapperUtil.instance().readerFor(t.getClass()).readValue(injected);
        T o = JacksonMapperUtil.instance().readerFor(t.getClass()).createParser(injected).readValueAs((Class<T>)t.getClass());
        return o;
    }

}
