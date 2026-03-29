package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.vault.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class InjectionService {

    private final JacksonService jacksonService;
    private final SystemVariableService systemVariableService;

    public <T> T injectFromVault(final VaultService vaultService, final T t) {
        return inject(t, vaultService::inject);
    }

    public <T> T injectFromSystem(final T t) {
        return inject(t, systemVariableService::inject);
    }

    @SuppressWarnings("unchecked")
    public <T> T inject(final T t, final Function<String, String> injector) {
        String asJson = jacksonService.writeValueToCopiedString(t);
        String injected = injector.apply(asJson);
        return jacksonService.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
