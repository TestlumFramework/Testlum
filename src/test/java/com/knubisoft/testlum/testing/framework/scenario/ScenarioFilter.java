package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.testlum.testing.model.global_config.TagValue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_ENABLED_TAGS_CONFIG;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_SCENARIOS_FILTERED_BY_TAGS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.STOP_IF_NON_PARSED_SCENARIO;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VALID_SCENARIOS_NOT_FOUND;

public class ScenarioFilter {

    public Set<MappingResult> filterScenarios(final GlobalTestConfigurationProvider configurationProvider,
                                              final Set<MappingResult> original) {
        boolean containsNonParsed = original.removeIf(this::isScenarioNonParsed);
        if (configurationProvider.provide().isStopIfInvalidScenario() && containsNonParsed) {
            throw new DefaultFrameworkException(STOP_IF_NON_PARSED_SCENARIO);
        } else if (original.isEmpty()) {
            throw new DefaultFrameworkException(VALID_SCENARIOS_NOT_FOUND);
        }
        return filterValidScenarios(original, configurationProvider);
    }

    private Set<MappingResult> filterValidScenarios(final Set<MappingResult> validScenarios,
                                                    final GlobalTestConfigurationProvider configurationProvider) {
        Set<MappingResult> activeScenarios = filterIsActive(validScenarios);
        Set<MappingResult> scenariosWithOnlyThisEnabled = filterScenariosIfOnlyThis(activeScenarios);
        return scenariosWithOnlyThisEnabled.isEmpty()
                ? filterScenariosByTags(activeScenarios, configurationProvider)
                : filterScenariosByTags(scenariosWithOnlyThisEnabled, configurationProvider);
    }

    private Set<MappingResult> filterIsActive(final Set<MappingResult> original) {
        return filterBy(original, e -> e.scenario.getSettings().isActive());
    }

    private Set<MappingResult> filterScenariosIfOnlyThis(final Set<MappingResult> original) {
        return filterBy(original, e -> e.scenario.getSettings().isOnlyThis());
    }

    private Set<MappingResult> filterScenariosByTags(final Set<MappingResult> activeScenarios,
                                                     final GlobalTestConfigurationProvider configurationProvider) {
        RunScenariosByTag runScenariosByTag = configurationProvider.provide().getRunScenariosByTag();
        return runScenariosByTag.isEnabled()
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
        List<String> scenarioTags = Arrays.asList((entry.scenario.getSettings().getTags()).split(COMMA));
        return scenarioTags.stream().anyMatch(enabledTags::contains);
    }

    private List<String> getEnabledTags(final List<TagValue> tags) {
        List<String> enabledTags = tags.stream()
                .filter(TagValue::isEnabled)
                .map(TagValue::getName)
                .collect(Collectors.toList());
        if (enabledTags.isEmpty()) {
            throw new DefaultFrameworkException(NO_ENABLED_TAGS_CONFIG);
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
