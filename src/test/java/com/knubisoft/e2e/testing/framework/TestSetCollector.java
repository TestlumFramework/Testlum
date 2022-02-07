package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.parser.CSVParser;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.global_config.BrowserVersion;
import com.knubisoft.e2e.testing.model.global_config.FilterTags;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TestSetCollector {

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<ScenarioCollector.MappingResult> filtered = filterScenarios(result.get());

        List<Arguments> scenariosAsTestArguments = filtered.stream()
                .flatMap(this::getArguments)
                .collect(Collectors.toList());
        return scenariosAsTestArguments.stream();
    }

    private Stream<Arguments> getArguments(final ScenarioCollector.MappingResult entry) {
        BrowserVersion browserVersion = GlobalTestConfigurationProvider.provide().getUi().getBrowserVersion();
        String shortPath = entry.file.getPath()
                .replace(TestResourceSettings.getInstance().getScenariosFolder().toString(), StringUtils.EMPTY);

        List<Arguments> result = new ArrayList<>();
        browserVersion.getBrowserVersionElement().forEach(each -> enrichArgumentsList(entry, result, each, shortPath));
        return result.stream();
    }

    private void enrichArgumentsList(final ScenarioCollector.MappingResult entry,
                                     final List<Arguments> result,
                                     final String browserVersion,
                                     final String shortPath) {
        BrowserSettings settings = GlobalTestConfigurationProvider.provide().getUi().getBrowserSettings();
        if (checkScenarioWithoutVariations(entry)) {
            result.add(Arguments.of(shortPath, entry.file, entry, browserVersion, settings, new HashMap<>()));
        } else {
            List<Map<String, String>> variationList = getVariationList(entry);
            variationList.forEach(variation ->
                    result.add(Arguments.of(shortPath, entry.file, entry, browserVersion, settings, variation)));
        }
    }

    @NotNull
    private List<Map<String, String>> getVariationList(final ScenarioCollector.MappingResult entry) {
        String variations = entry.scenario.getVariations();
        return new CSVParser().parseVariations(variations);
    }

    private Set<ScenarioCollector.MappingResult> filterScenarios(final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> filtered = filterParsedScenarios(original);
        filtered = filterScenariosIfOnlyThis(filtered);
        filtered = filterAndSortByTags(filtered);

        Set<ScenarioCollector.MappingResult> result = getNonParsedScenarios(original);
        result.addAll(filtered);
        return result;
    }

    private Set<ScenarioCollector.MappingResult> filterScenariosIfOnlyThis(
            final Set<ScenarioCollector.MappingResult> original) {
        Set<ScenarioCollector.MappingResult> filtered = original.stream()
                .filter(this::filterIsActive)
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

    private Set<ScenarioCollector.MappingResult> filterParsedScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(this::isScenarioParsed)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<ScenarioCollector.MappingResult> getNonParsedScenarios(
            final Set<ScenarioCollector.MappingResult> original) {
        return original.stream()
                .filter(e -> !isScenarioParsed(e))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isScenarioParsed(final ScenarioCollector.MappingResult entry) {
        return Objects.nonNull(entry.scenario);
    }

    private boolean checkScenarioWithoutVariations(final ScenarioCollector.MappingResult entry) {
        return !isScenarioParsed(entry) || Objects.isNull(entry.scenario.getVariations());
    }
}
