package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsImpl.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.vault.VaultService;
import lombok.SneakyThrows;

import java.util.Map;

public class InjectionUtil {

    private InjectionUtil() {
        // nop
    }

    @SuppressWarnings("unchecked")
    public static <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = GlobalVariationsProvider.getValue(asJson, variation);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T injectObjectVariation(final T t,
                                       final Map<String, String> variation,
                                       final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = GlobalVariationsProvider.getValue(asJson, variation, scenarioContext);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T injectFromVault(final VaultService vaultService, final T t) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = vaultService.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T injectFromSystem(final T t) {
        String asJason = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = SystemVariableService.inject(asJason);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
