package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class ScenarioInjectionUtil {

    private final GlobalVariationsProvider globalVariationsProvider;
    private final InjectionService injectionService;

    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        return injectionService.inject(t, scenarioContext::inject);
    }

    public <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        return injectionService.inject(t, s -> globalVariationsProvider.getValue(s, variation));
    }

    public <T> T injectObjectVariation(final T t, final Map<String, String> variation, final ScenarioContext ctx) {
        return injectionService.inject(t, s -> globalVariationsProvider.getValue(s, variation, ctx));
    }

}
