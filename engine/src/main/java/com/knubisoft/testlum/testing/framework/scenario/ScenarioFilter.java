package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.global_config.RunScenariosByTag;
import com.knubisoft.testlum.testing.model.global_config.TagValue;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_ENABLED_TAGS_CONFIG;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_SCENARIOS_FILTERED_BY_TAGS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.STOP_IF_NON_PARSED_SCENARIO;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VALID_SCENARIOS_NOT_FOUND;

public class ScenarioFilter {

    //CHECKSTYLE:OFF
    public List<MappingResult> filterScenarios(final List<MappingResult> original) {
        List<MappingResult> nonParsedScenarios =
                original.stream().filter(e -> e.scenario == null).toList();
        if (!nonParsedScenarios.isEmpty()) {
            for (MappingResult entry : nonParsedScenarios) {
                LogUtil.logNonParsedScenarioInfo(entry.file.getPath(), entry.exception.getMessage());
            }
            if (GlobalTestConfigurationProvider.get().provide().isStopIfInvalidScenario()) {
                throw new DefaultFrameworkException(STOP_IF_NON_PARSED_SCENARIO);
            }
        } else if (original.isEmpty()) {
            throw new DefaultFrameworkException(VALID_SCENARIOS_NOT_FOUND);
        }
        List<MappingResult> originalWithoutNonParsed = new ArrayList<>(original);
        originalWithoutNonParsed.removeAll(nonParsedScenarios);
        return filterValidScenarios(originalWithoutNonParsed);
    }
    //CHECKSTYLE:ON

    private List<MappingResult> filterValidScenarios(final List<MappingResult> validScenarios) {
        List<MappingResult> activeScenarios = filterIsActive(validScenarios);
        List<MappingResult> scenariosWithOnlyThisEnabled = filterScenariosIfOnlyThis(activeScenarios);
        return filterScenariosByTags(scenariosWithOnlyThisEnabled.isEmpty()
                ? activeScenarios : scenariosWithOnlyThisEnabled);
    }

    private List<MappingResult> filterIsActive(final List<MappingResult> original) {
        return filterBy(original, e -> e.scenario.getSettings().isActive());
    }

    private List<MappingResult> filterScenariosIfOnlyThis(final List<MappingResult> original) {
        return filterBy(original, e -> e.scenario.getSettings().isOnlyThis());
    }

    private List<MappingResult> filterScenariosByTags(final List<MappingResult> activeScenarios) {
        RunScenariosByTag runScenariosByTag = GlobalTestConfigurationProvider.get().provide().getRunScenariosByTag();
        return runScenariosByTag.isEnabled()
                ? filterByTags(activeScenarios, getEnabledTags(runScenariosByTag.getTag()))
                : sortByName(activeScenarios);
    }

    private List<MappingResult> filterByTags(final List<MappingResult> original, final List<String> enabledTags) {
        List<MappingResult> filteredByTags = filterBy(sortByName(original), e -> isMatchesTags(e, enabledTags)).stream()
                .sorted(Comparator.comparing(mappingResult -> mappingResult.scenario.getSettings().getTags()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (filteredByTags.isEmpty()) {
            throw new DefaultFrameworkException(NO_SCENARIOS_FILTERED_BY_TAGS);
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
            LogUtil.logScenarioWithoutTags(entry.file.getPath());
            return false;
        }
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

    private List<MappingResult> filterBy(final List<MappingResult> scenarios,
                                         final Predicate<MappingResult> by) {
        return scenarios.stream().filter(by).collect(Collectors.toCollection(ArrayList::new));
    }
}
