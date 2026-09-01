package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.exception.IntegrationDisabledException;
import com.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.testlum.testing.framework.util.LogUtil;
import com.testlum.testing.logger.ConfigurationLogger;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.TagValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ScenarioFilter {

    private final GlobalTestConfiguration globalTestConfiguration;
    private final LogUtil logUtil;
    private final ConfigurationLogger configurationLogger;

    public List<MappingResult> filterScenarios(final List<MappingResult> original) {
        configurationLogger.logTagConfiguration(globalTestConfiguration.getRunScenariosByTag(),
                countScenariosByTag(original));
        List<MappingResult> nonParsedScenarios =
                original.stream().filter(e -> e.scenario == null).toList();
        handleNonParsedScenarios(nonParsedScenarios, original.isEmpty());
        List<MappingResult> originalWithoutNonParsed = new ArrayList<>(original);
        originalWithoutNonParsed.removeAll(nonParsedScenarios);
        List<MappingResult> selectedScenarios = filterValidScenarios(originalWithoutNonParsed);
        return handleInvalidScenarios(selectedScenarios);
    }

    private List<MappingResult> handleInvalidScenarios(final List<MappingResult> selectedScenarios) {
        registerInvalidScenarios(selectedScenarios);
        List<MappingResult> runnableScenarios = filterBy(selectedScenarios,
                e -> e.exception == null || e.exception instanceof IntegrationDisabledException);
        if (runnableScenarios.size() < selectedScenarios.size()
            && globalTestConfiguration.isStopIfInvalidScenario()) {
            throw new DefaultFrameworkException(ExceptionMessage.STOP_IF_NON_PARSED_SCENARIO);
        }
        if (runnableScenarios.isEmpty() && !selectedScenarios.isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.VALID_SCENARIOS_NOT_FOUND);
        }
        return runnableScenarios;
    }

    private void registerInvalidScenarios(final List<MappingResult> selectedScenarios) {
        selectedScenarios.stream()
                .filter(e -> e.exception != null)
                .forEach(e -> ScenarioStatusRegistry.registerInvalid(
                        e.file, e.exception.getMessage()));
    }

    private void handleNonParsedScenarios(final List<MappingResult> nonParsed, final boolean originalEmpty) {
        if (!nonParsed.isEmpty()) {
            nonParsed.forEach(entry -> ScenarioStatusRegistry.registerInvalid(
                    entry.file, entry.exception.getMessage()));
            if (globalTestConfiguration.isStopIfInvalidScenario()) {
                throw new DefaultFrameworkException(ExceptionMessage.STOP_IF_NON_PARSED_SCENARIO);
            }
        } else if (originalEmpty) {
            throw new DefaultFrameworkException(ExceptionMessage.VALID_SCENARIOS_NOT_FOUND);
        }
    }

    private List<MappingResult> filterValidScenarios(final List<MappingResult> validScenarios) {
        List<MappingResult> activeScenarios = filterIsActive(validScenarios);
        List<MappingResult> scenariosWithOnlyThisEnabled = filterScenariosIfOnlyThis(activeScenarios);
        if (scenariosWithOnlyThisEnabled.isEmpty()) {
            return filterScenariosByTags(activeScenarios);
        }
        registerSkippedByOnlyThis(activeScenarios, scenariosWithOnlyThisEnabled);
        return filterScenariosByTags(scenariosWithOnlyThisEnabled);
    }

    private List<MappingResult> filterIsActive(final List<MappingResult> original) {
        return filterBy(original, e -> e.scenario.getSettings().isActive(),
                e -> ScenarioStatusRegistry.registerSkipped(e.file, LogMessage.SCENARIO_SKIPPED_INACTIVE));
    }

    private List<MappingResult> filterScenariosIfOnlyThis(final List<MappingResult> original) {
        return filterBy(original, e -> e.scenario.getSettings().isOnlyThis());
    }

    private void registerSkippedByOnlyThis(final List<MappingResult> activeScenarios,
                                           final List<MappingResult> scenariosWithOnlyThisEnabled) {
        activeScenarios.stream()
                .filter(e -> !scenariosWithOnlyThisEnabled.contains(e))
                .forEach(e -> ScenarioStatusRegistry.registerSkipped(
                        e.file, LogMessage.SCENARIO_SKIPPED_ONLY_THIS));
    }

    private List<MappingResult> filterScenariosByTags(final List<MappingResult> activeScenarios) {
        RunScenariosByTag runScenariosByTag = globalTestConfiguration.getRunScenariosByTag();
        return runScenariosByTag.isEnabled()
                ? filterByTags(activeScenarios, getEnabledTags(runScenariosByTag.getTag()))
                : sortByName(activeScenarios);
    }

    private List<MappingResult> filterByTags(final List<MappingResult> original, final List<String> enabledTags) {
        List<MappingResult> filteredByTags = filterBy(sortByName(original), e -> isMatchesTags(e, enabledTags)).stream()
                .sorted(Comparator.comparing(mappingResult -> mappingResult.scenario.getSettings().getTags()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (filteredByTags.isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.NO_SCENARIOS_FILTERED_BY_TAGS);
        }
        return filteredByTags;
    }

    private List<MappingResult> sortByName(final List<MappingResult> activeScenarios) {
        return activeScenarios.stream()
                .sorted(Comparator.comparing(mappingResult -> mappingResult.file.getPath()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isMatchesTags(final MappingResult entry, final List<String> enabledTags) {
        if (entry.scenario.getSettings().getTags() == null) {
            logUtil.logScenarioWithoutTags(entry.file.getPath());
            ScenarioStatusRegistry.registerSkipped(entry.file, LogMessage.SCENARIO_SKIPPED_WITHOUT_TAGS);
            return false;
        }
        List<String> scenarioTags =
                Arrays.asList((entry.scenario.getSettings().getTags()).split(DelimiterConstant.COMMA));
        boolean matches = scenarioTags.stream().anyMatch(enabledTags::contains);
        if (!matches) {
            ScenarioStatusRegistry.registerSkipped(entry.file, LogMessage.SCENARIO_SKIPPED_TAGS_NOT_MATCH);
        }
        return matches;
    }

    private Map<String, Long> countScenariosByTag(final List<MappingResult> scenarios) {
        return scenarios.stream()
                .filter(entry -> entry.scenario != null)
                .map(entry -> entry.scenario.getSettings().getTags())
                .filter(Objects::nonNull)
                .flatMap(tags -> Arrays.stream(tags.split(DelimiterConstant.COMMA)))
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
    }

    private List<String> getEnabledTags(final List<TagValue> tags) {
        List<String> enabledTags = tags.stream()
                .filter(TagValue::isEnabled)
                .map(TagValue::getName)
                .toList();
        if (enabledTags.isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.NO_ENABLED_TAGS_CONFIG);
        }
        return enabledTags;
    }

    private List<MappingResult> filterBy(final List<MappingResult> scenarios,
                                         final Predicate<MappingResult> by) {
        return scenarios.stream().filter(by).collect(Collectors.toCollection(ArrayList::new));
    }

    private List<MappingResult> filterBy(final List<MappingResult> scenarios,
                                         final Predicate<MappingResult> by,
                                         final Consumer<MappingResult> onRejected) {
        List<MappingResult> accepted = new ArrayList<>(scenarios.size());
        for (MappingResult scenario : scenarios) {
            if (by.test(scenario)) {
                accepted.add(scenario);
            } else {
                onRejected.accept(scenario);
            }
        }
        return accepted;
    }
}
