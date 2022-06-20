package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.global_config.RunScriptsByTag;
import com.knubisoft.e2e.testing.model.global_config.TagValue;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ScenarioFilter {

    public FiltrationResult filterScenarios(final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> activeScenarios = original.stream().filter(ScenarioFilter::filterIsActive)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<ScenarioCollector.MappingResult> invalidScenarios = getNonParsedScenarios(original);
        if (invalidScenarios.size() == original.size()) {
            return new FiltrationResult(Collections.emptySet(), invalidScenarios, true);
        }
        return new FiltrationResult(filterValidScenarios(activeScenarios), invalidScenarios, false);
    }

    private Set<ScenarioCollector.MappingResult> filterValidScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> parsedScenarios = filterParsedScenarios(original);
        Set<ScenarioCollector.MappingResult> validScenarios = filterScenariosIfOnlyThis(parsedScenarios);
        if (validScenarios.isEmpty()){
            validScenarios = filterAndSortByTags(parsedScenarios);
        }
        return validScenarios;
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

    private boolean filterIsActive(final ScenarioCollector.MappingResult entry) {
        Scenario scenario = entry.scenario;
        if (scenario != null){
            return scenario.isActive();
        }
        return false;
    }

    private Set<ScenarioCollector.MappingResult> filterAndSortByTags(
            final Set<ScenarioCollector.MappingResult> original) {
        RunScriptsByTag tags = GlobalTestConfigurationProvider.provide().getRunScriptsByTag();
        List<String> enabledTags = tags.getValue().stream().filter(TagValue::isEnable)
                .map(TagValue::getTag).collect(Collectors.toList());
        if (tags.isEnable()) {
            if (enabledTags.isEmpty()) {
                throw new DefaultFrameworkException("There are no active tags in runScriptByTag");
            }
            Set<ScenarioCollector.MappingResult> filtered = filterByTags(original, enabledTags);
            return sortByTags(filtered, enabledTags);
        }
        return original;
    }

    private Set<ScenarioCollector.MappingResult> filterByTags(final Set<ScenarioCollector.MappingResult> original,
                                                              final List<String> enabledTags) {
        Set<ScenarioCollector.MappingResult> filtered = original.stream().filter(m -> isMatchesTestTags(m, enabledTags))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (filtered.isEmpty()){
            throw new DefaultFrameworkException("There are no active scenarios by enabled tags");
        }
        return filtered;
    }

    private boolean isMatchesTestTags(final ScenarioCollector.MappingResult entry, final List<String> enabledTags) {
        List<String> scenarioTags = entry.scenario.getTags().getTag();
        return scenarioTags.stream().anyMatch(enabledTags::contains);
    }

    private Set<ScenarioCollector.MappingResult> sortByTags(final Set<ScenarioCollector.MappingResult> original,
                                                            final List<String> enabledTags) {
        return original.stream().sorted(Comparator.comparing(e -> getScenarioTagOrderIndex(e, enabledTags)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Integer getScenarioTagOrderIndex(final ScenarioCollector.MappingResult entry, final List<String> tags) {
        return entry.scenario.getTags().getTag()
                .stream()
                .map(tags::indexOf)
                .min(Integer::compareTo)
                .orElse(0);
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
