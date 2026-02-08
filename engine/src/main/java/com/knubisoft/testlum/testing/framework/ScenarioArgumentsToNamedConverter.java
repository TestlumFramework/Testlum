package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class ScenarioArgumentsToNamedConverter implements ArgumentConverter {

    @Override
    public Object convert(final Object source, final ParameterContext context) {
        if (source instanceof ScenarioArguments scenarioArguments) {
            return Named.of(scenarioArguments.getPath(), scenarioArguments);
        }
        throw new DefaultFrameworkException("Cannot convert %s to Named".formatted(source));
    }
}