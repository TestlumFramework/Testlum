package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.vault.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class InjectionUtil {

    private final GlobalVariationsProvider globalVariationsProvider;
    private final JacksonService jacksonService;
    private final SystemVariableService systemVariableService;

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = jacksonService.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return jacksonService.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = jacksonService.writeValueToCopiedString(t);
        String injected = globalVariationsProvider.getValue(asJson, variation);
        return jacksonService.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T injectObjectVariation(final T t,
                                       final Map<String, String> variation,
                                       final ScenarioContext scenarioContext) {
        String asJson = jacksonService.writeValueToCopiedString(t);
        String injected = globalVariationsProvider.getValue(asJson, variation, scenarioContext);
        return jacksonService.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T injectFromVault(final VaultService vaultService, final T t) {
        String asJson = jacksonService.writeValueToCopiedString(t);
        String injected = vaultService.inject(asJson);
        return jacksonService.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T injectFromSystem(final T t) {
        String asJason = jacksonService.writeValueToCopiedString(t);
        String injected = systemVariableService.inject(asJason);
        return jacksonService.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
