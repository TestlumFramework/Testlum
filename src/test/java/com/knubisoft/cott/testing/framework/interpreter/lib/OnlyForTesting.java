package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.google.common.base.Preconditions;
import com.knubisoft.cott.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.cott.testing.framework.interpreter.lib.invoke.AbstractInvokeSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

// TODO remove this class later, just for demo invoking
public class OnlyForTesting extends AbstractInvokeSupplier {

    @Autowired
    private ApiClient apiClient;

    @Override
    public Object get() {
        Preconditions.checkNotNull(apiClient);
        return new Test("ABC");
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class Test {
        private final String name;
    }
}
