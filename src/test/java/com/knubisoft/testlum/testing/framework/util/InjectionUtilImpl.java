package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.interpreter.InjectionUtil;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsImpl;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InjectionUtilImpl implements InjectionUtil {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = GlobalVariationsImpl.getVariationValue(asJson, variation);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
