package com.knubisoft.cott.testing.framework.scenario;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class FiltrationResult {

    private final Set<ScenarioCollector.MappingResult> scenariosWithUiSteps;
    private final Set<ScenarioCollector.MappingResult> scenariosWithoutUiSteps;
    private final boolean onlyScenariosWithoutUiSteps;
}
