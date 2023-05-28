package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.databind.JavaType;
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
        Class<T> aClass = (Class<T>) t.getClass();
        JavaType javaType = JacksonMapperUtil.instance().getTypeFactory().constructType(aClass);
//        return JacksonMapperUtil.readValue(injected, (Class<T>) t.getClass());
        T o = (T) JacksonMapperUtil.instance().readerFor(javaType).createParser(injected).readValueAs(t.getClass());
        return o;
    }

}
