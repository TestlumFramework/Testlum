package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.framework.vaultService.VaultService;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class InjectionUtil {

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = GlobalVariations.getVariationValue(asJson, variation);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SneakyThrows
    public Integrations injectFromVault(final Integrations integrations) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(integrations);
        String injected = VaultService.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, integrations.getClass());
    }
}
