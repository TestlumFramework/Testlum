package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.e2e.testing.model.global_config.TagValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.NO_ACTIVE_SCENARIOS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NO_ENABLE_TAGS_LOG;

@Slf4j
@UtilityClass
public class ScenarioFilter {

    public FiltrationResult filterScenarios(final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> invalidScenarios = getNonParsedScenarios(original);
        if (invalidScenarios.size() == original.size()) {
            return new FiltrationResult(Collections.emptySet(), invalidScenarios, true);
        }
        return new FiltrationResult(filterValidScenarios(original), invalidScenarios, false);
    }

    private Set<ScenarioCollector.MappingResult> filterValidScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> validScenarios = filterParsedScenarios(original);
        Set<ScenarioCollector.MappingResult> activeScenarios = filterIsActive(validScenarios);
        Set<ScenarioCollector.MappingResult> scenariosWithOnlyThisEnabled = filterScenariosIfOnlyThis(activeScenarios);
        RunScenariosByTag runScenariosByTag = GlobalTestConfigurationProvider.provide().getRunScenariosByTag();
        return scenariosWithOnlyThisEnabled.isEmpty() ? runScenariosByTag.isEnable()
                ? filterByTags(activeScenarios, getEnabledTags(runScenariosByTag.getTag())) : activeScenarios
                : scenariosWithOnlyThisEnabled;
    }

    private Set<ScenarioCollector.MappingResult> filterParsedScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(ScenarioFilter::isScenarioParsed)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<ScenarioCollector.MappingResult> getNonParsedScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(e -> !isScenarioParsed(e))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<ScenarioCollector.MappingResult> filterScenariosIfOnlyThis(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(e -> e.scenario.isOnlyThis())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<ScenarioCollector.MappingResult> filterIsActive(Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(e -> e.scenario.isActive())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    private Set<ScenarioCollector.MappingResult> filterByTags(final Set<ScenarioCollector.MappingResult> original,
                                                              final List<String> enabledTags) {
        Set<ScenarioCollector.MappingResult> filtered = original.stream().filter(e -> isMatchesTags(e, enabledTags))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (filtered.isEmpty()){
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


    private boolean isScenarioParsed(final ScenarioCollector.MappingResult entry) {
        return Objects.nonNull(entry.scenario);
    }

    @Getter
    @AllArgsConstructor
    public static class FiltrationResult {
        private Set<ScenarioCollector.MappingResult> validScenarios;
        private Set<ScenarioCollector.MappingResult> invalidScenarios;
        private boolean onlyInvalidScenarios;
    }
}
