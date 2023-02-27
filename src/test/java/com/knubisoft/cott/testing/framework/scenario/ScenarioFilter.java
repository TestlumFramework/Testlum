package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.cott.testing.model.global_config.TagValue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NO_ENABLE_TAGS_CONFIG;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NO_SCENARIOS_FILTERED_BY_TAGS;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.STOP_IF_NON_PARSED_SCENARIO;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VALID_SCENARIOS_NOT_FOUND;

public class ScenarioFilter {

    public Set<MappingResult> filterScenarios(final Set<MappingResult> original) {
        boolean containsNonParsed = original.removeIf(this::isScenarioNonParsed);
        if (GlobalTestConfigurationProvider.provide().isStopIfInvalidScenario() && containsNonParsed) {
            throw new DefaultFrameworkException(STOP_IF_NON_PARSED_SCENARIO);
        } else if (original.isEmpty()) {
            throw new DefaultFrameworkException(VALID_SCENARIOS_NOT_FOUND);
        }
        return filterValidScenarios(original);
    }

    private Set<MappingResult> filterValidScenarios(final Set<MappingResult> validScenarios) {
        Set<MappingResult> activeScenarios = filterIsActive(validScenarios);
        Set<MappingResult> scenariosWithOnlyThisEnabled = filterScenariosIfOnlyThis(activeScenarios);
        return scenariosWithOnlyThisEnabled.isEmpty()
                ? filterScenariosByTags(activeScenarios)
                : filterScenariosByTags(scenariosWithOnlyThisEnabled);
    }

    private Set<MappingResult> filterIsActive(final Set<MappingResult> original) {
        return filterBy(original, e -> e.scenario.isActive());
    }

    private Set<MappingResult> filterScenariosIfOnlyThis(final Set<MappingResult> original) {
        return filterBy(original, e -> e.scenario.isOnlyThis());
    }

    private Set<MappingResult> filterScenariosByTags(final Set<MappingResult> activeScenarios) {
        RunScenariosByTag runScenariosByTag = GlobalTestConfigurationProvider.provide().getRunScenariosByTag();
        return runScenariosByTag.isEnable()
                ? filterByTags(activeScenarios, getEnabledTags(runScenariosByTag.getTag()))
                : activeScenarios;
    }

    private Set<MappingResult> filterByTags(final Set<MappingResult> original, final List<String> enabledTags) {
        Set<MappingResult> filteredByTags = filterBy(original, e -> isMatchesTags(e, enabledTags));
        if (filteredByTags.isEmpty()) {
            throw new DefaultFrameworkException(NO_SCENARIOS_FILTERED_BY_TAGS);
        }
        return filteredByTags;
    }

    private boolean isMatchesTags(final MappingResult entry, final List<String> enabledTags) {
        List<String> scenarioTags = entry.scenario.getTags().getTag();
        return scenarioTags.stream().anyMatch(enabledTags::contains);
    }

    private List<String> getEnabledTags(final List<TagValue> tags) {
        List<String> enabledTags = tags.stream()
                .filter(TagValue::isEnable)
                .map(TagValue::getName)
                .collect(Collectors.toList());
        if (enabledTags.isEmpty()) {
            throw new DefaultFrameworkException(NO_ENABLE_TAGS_CONFIG);
        }
        return enabledTags;
    }

    private LinkedHashSet<MappingResult> filterBy(final Set<MappingResult> scenarios,
                                                  final Predicate<MappingResult> by) {
        return scenarios.stream()
                .filter(by)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isScenarioNonParsed(final MappingResult entry) {
        if (Objects.nonNull(entry.scenario)) {
            return false;
        }
        LogUtil.logNonParsedScenarioInfo(entry.file.getPath(), entry.exception.getMessage());
        return true;
    }
}
