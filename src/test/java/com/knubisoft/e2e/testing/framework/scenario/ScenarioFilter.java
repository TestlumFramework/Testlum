package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.FilterTags;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import com.knubisoft.e2e.testing.model.scenario.Ui;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ScenarioFilter {

    public Set<ScenarioCollector.MappingResult> filterScenarios(final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> filtered = filterParsedScenarios(original);
        filtered = filterScenariosIfOnlyThis(filtered);
        filtered = filterAndSortByTags(filtered);

        Set<ScenarioCollector.MappingResult> result = getNonParsedScenarios(original);
        result.addAll(filtered);
        return result;
    }


    public Set<ScenarioCollector.MappingResult> filterScenarioByType(final ScenarioType scenarioType,
            final Set<ScenarioCollector.MappingResult> original) {
        if (scenarioType == ScenarioType.UI) {
            return filterOnlyUiScenarios(original);
        } else if (scenarioType == ScenarioType.BACKEND) {
            return filterOnlyBackendScenarios(original);
        } else {
            throw new UnsupportedOperationException("Scenario type not supported");
        }
    }

    private Set<ScenarioCollector.MappingResult> filterOnlyBackendScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(ScenarioFilter::scenarioHasNotUiTestingSteps)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<ScenarioCollector.MappingResult> filterOnlyUiScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(ScenarioFilter::scenarioHasUiTestingSteps)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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

    private boolean scenarioHasNotUiTestingSteps(final ScenarioCollector.MappingResult entry) {
        Scenario scenario = entry.scenario;
        return scenario.getCommands().stream().noneMatch(command -> command instanceof Ui);
    }

    private boolean scenarioHasUiTestingSteps(final ScenarioCollector.MappingResult entry) {
        Scenario scenario = entry.scenario;
        return scenario.getCommands().stream().anyMatch(command -> command instanceof Ui);
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

    public enum ScenarioType {
        UI,
        BACKEND
    }
}
