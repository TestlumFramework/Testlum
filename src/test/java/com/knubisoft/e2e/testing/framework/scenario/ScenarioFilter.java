package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.FilterTags;
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
        Set<ScenarioCollector.MappingResult> invalidScenarios = getNonParsedScenarios(original);
        if (invalidScenarios.size() == original.size()) {
            return new FiltrationResult(Collections.emptySet(), invalidScenarios, true);
        }
        return new FiltrationResult(filterValidScenarios(original), invalidScenarios, false);
    }

    private Set<ScenarioCollector.MappingResult> filterValidScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> validScenarios = filterParsedScenarios(original);
        validScenarios = filterScenariosIfOnlyThis(validScenarios);
        validScenarios = filterAndSortByTags(validScenarios);
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
        Set<ScenarioCollector.MappingResult> filtered = original.stream()
                .filter(ScenarioFilter::filterIsActive)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return filtered.isEmpty() ? original : filtered;
    }

    private boolean filterIsActive(final ScenarioCollector.MappingResult entry) {
        Scenario scenario = entry.scenario;
        return scenario.isActive() && scenario.isOnlyThis();
    }

    private Set<ScenarioCollector.MappingResult> filterAndSortByTags(
            final Set<ScenarioCollector.MappingResult> original) {
        FilterTags tags = GlobalTestConfigurationProvider.provide().getFilterTags();
        if (tags.isEnable()) {
            Set<ScenarioCollector.MappingResult> filtered = filterByTags(original, tags);
            return tags.isEnableOrder() ? sortByTags(filtered, tags) : filtered;
        }
        return original;
    }

    private Set<ScenarioCollector.MappingResult> filterByTags(final Set<ScenarioCollector.MappingResult> original,
                                                              final FilterTags tags) {
        Set<ScenarioCollector.MappingResult> filtered = original.stream().filter(m -> isMatchesTestTags(m, tags))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return filtered.isEmpty() ? original : filtered;
    }

    private boolean isMatchesTestTags(final ScenarioCollector.MappingResult entry, final FilterTags tags) {
        List<String> scenarioTags = entry.scenario.getTags().getTag();
        return scenarioTags.stream().anyMatch(tags.getValue()::contains);
    }

    private Set<ScenarioCollector.MappingResult> sortByTags(final Set<ScenarioCollector.MappingResult> original,
                                                            final FilterTags tags) {
        return original.stream().sorted(Comparator.comparing(e -> getScenarioTagOrderIndex(e, tags.getValue())))
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
