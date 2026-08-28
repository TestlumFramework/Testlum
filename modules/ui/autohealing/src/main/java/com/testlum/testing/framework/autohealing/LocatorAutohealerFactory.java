package com.testlum.testing.framework.autohealing;

import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import org.springframework.stereotype.Component;

@Component
public class LocatorAutohealerFactory implements AutoHealerFactory {

    @Override
    public AutoHealer create(final ExecutorDependencies dependencies) {
        return new LocatorAutohealer(dependencies);
    }
}
