package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.cott.testing.model.global_config.TagValue;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NO_ACTIVE_SCENARIOS_LOG;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NO_ENABLE_TAGS_LOG;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VALID_SCENARIOS_NOT_FOUND;

@Slf4j
@UtilityClass
public class ScenarioFilter {

    public FiltrationResult filterScenarios(final Set<ScenarioCollector.MappingResult> original) {
        original.removeIf(ScenarioFilter::isScenarioNonParsed);
        if (original.isEmpty()) {
            throw new DefaultFrameworkException(VALID_SCENARIOS_NOT_FOUND);
        }
        Set<ScenarioCollector.MappingResult> validScenarios = filterValidScenarios(original);
        return splitScenariosByType(validScenarios);
    }

    private FiltrationResult splitScenariosByType(final Set<ScenarioCollector.MappingResult> validScenarios) {
        Set<ScenarioCollector.MappingResult> scenariosWithoutUiSteps = filterScenariosWithoutUiSteps(validScenarios);
        if (validScenarios.size() == scenariosWithoutUiSteps.size()) {
            return new FiltrationResult(Collections.emptySet(), scenariosWithoutUiSteps, true);
        }
        Set<ScenarioCollector.MappingResult> scenariosWithUiSteps =
                filterBy(validScenarios, e -> scenarioContainsUiSteps(e.scenario));
        return new FiltrationResult(scenariosWithUiSteps, scenariosWithoutUiSteps, false);
    }

    private Set<ScenarioCollector.MappingResult> filterValidScenarios(
            final Set<ScenarioCollector.MappingResult> validScenarios) {
        Set<ScenarioCollector.MappingResult> activeScenarios = filterIsActive(validScenarios);
        Set<ScenarioCollector.MappingResult> scenariosWithOnlyThisEnabled = filterScenariosIfOnlyThis(activeScenarios);
        return scenariosWithOnlyThisEnabled.isEmpty()
                ? scenariosByTags(activeScenarios)
                : scenariosWithOnlyThisEnabled;
    }

    private Set<ScenarioCollector.MappingResult> filterScenariosIfOnlyThis(
            final Set<ScenarioCollector.MappingResult> original) {
        return filterBy(original, e -> e.scenario.isOnlyThis());
    }

    private Set<ScenarioCollector.MappingResult> filterIsActive(final Set<ScenarioCollector.MappingResult> original) {
        return filterBy(original, e -> e.scenario.isActive());
    }

    private Set<ScenarioCollector.MappingResult> scenariosByTags(
            final Set<ScenarioCollector.MappingResult> activeScenarios) {
        RunScenariosByTag runScenariosByTag = GlobalTestConfigurationProvider.provide().getRunScenariosByTag();
        return runScenariosByTag.isEnable()
                ? filterByTags(activeScenarios, getEnabledTags(runScenariosByTag.getTag()))
                : activeScenarios;
    }

    private Set<ScenarioCollector.MappingResult> filterByTags(final Set<ScenarioCollector.MappingResult> original,
                                                              final List<String> enabledTags) {
        Set<ScenarioCollector.MappingResult> filtered = filterBy(original, e -> isMatchesTags(e, enabledTags));
        if (filtered.isEmpty()) {
            throw new DefaultFrameworkException(NO_ACTIVE_SCENARIOS_LOG);
        }
        return filtered;
    }

    private boolean isMatchesTags(final ScenarioCollector.MappingResult entry, final List<String> enabledTags) {
        List<String> scenarioTags = entry.scenario.getTags().getTag();
        return scenarioTags.stream().anyMatch(enabledTags::contains);
    }

    private List<String> getEnabledTags(final List<TagValue> tags) {
        List<String> enabledTags = tags.stream().filter(TagValue::isEnable)
                .map(TagValue::getName).collect(Collectors.toList());
        if (enabledTags.isEmpty()) {
            throw new DefaultFrameworkException(NO_ENABLE_TAGS_LOG);
        }
        return enabledTags;
    }

    private Set<ScenarioCollector.MappingResult> filterScenariosWithoutUiSteps(
            final Set<ScenarioCollector.MappingResult> scenarios) {
        return filterBy(scenarios, e -> !scenarioContainsUiSteps(e.scenario));
    }

    private LinkedHashSet<ScenarioCollector.MappingResult> filterBy(
            final Set<ScenarioCollector.MappingResult> scenarios,
            final Predicate<ScenarioCollector.MappingResult> by) {
        return scenarios.stream()
                .filter(by)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean scenarioContainsUiSteps(final Scenario scenario) {
        return scenario.getCommands().stream()
                .anyMatch(command -> command instanceof com.knubisoft.cott.testing.model.scenario.Ui);
    }

    private boolean isScenarioNonParsed(final ScenarioCollector.MappingResult entry) {
        if (Objects.nonNull(entry.scenario)) {
            return false;
        }
        LogUtil.logNonParsedScenarioInfo(entry.file.getPath(), entry.exception.getMessage());
        return true;
    }
}
