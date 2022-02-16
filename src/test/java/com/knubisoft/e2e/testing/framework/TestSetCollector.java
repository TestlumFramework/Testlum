package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.parser.CSVParser;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.e2e.testing.model.ScenarioArguments;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.global_config.Ui;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@RequiredArgsConstructor
public class TestSetCollector {

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<ScenarioCollector.MappingResult> filteredScenarios = ScenarioFilter.filterScenarios(result.get());
        if (uiDisabled()) {
            return getScenarioArgumentsWithoutUIConfiguration(filteredScenarios);
        }
        return getScenarioArguments(filteredScenarios);
    }

    private Stream<Arguments> getScenarioArguments(final Set<ScenarioCollector.MappingResult> scenarios) {
        List<String> browserVersions = GlobalTestConfigurationProvider.getBrowserSettings().getVersions().getVersion();
        List<ScenarioArguments> scenarioArgumentsList = new ArrayList<>();
        scenarios.forEach(entry -> {
            if (scenarioContainsUISteps(entry.scenario)) {
                browserVersions.forEach(version ->
                        addScenarioArgumentsWithUIConfiguration(entry, version, scenarioArgumentsList));
            } else {
                scenarioArgumentsList.add(getArgumentsWithoutUIConfigurations(entry));
            }
        });
        return scenarioArgumentsList.stream().map(this::convertToNamedArguments);
    }

    private void addScenarioArgumentsWithUIConfiguration(final ScenarioCollector.MappingResult entry,
                                                final String browserVersion,
                                                final List<ScenarioArguments> arguments) {
        BrowserSettings browserSettings = GlobalTestConfigurationProvider.getBrowserSettings();
        if (variationsExist(entry)) {
            List<Map<String, String>> variationList = getVariationList(entry);
            variationList.forEach(variation ->
                    arguments.add(getArgumentsWithUIConfigurations(entry, browserVersion, browserSettings, variation)));
        } else {
            arguments.add(getArgumentsWithUIConfigurations(entry, browserVersion, browserSettings, new HashMap<>()));
        }
    }

    private Stream<Arguments> getScenarioArgumentsWithoutUIConfiguration(
            final Set<ScenarioCollector.MappingResult> scenarios) {
        return scenarios.stream()
                .map(this::getArgumentsWithoutUIConfigurations)
                .map(this::convertToNamedArguments);
    }

    private ScenarioArguments getArgumentsWithUIConfigurations(final ScenarioCollector.MappingResult entry,
                                                               final String browserVersion,
                                                               final BrowserSettings settings,
                                                               final Map<String, String> variation) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .browserVersion(browserVersion)
                .browserSettings(settings)
                .variation(variation)
                .containsUiSteps(true)
                .build();
    }

    private ScenarioArguments getArgumentsWithoutUIConfigurations(final ScenarioCollector.MappingResult entry) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .variation(new HashMap<>())
                .build();
    }

    private Arguments convertToNamedArguments(final ScenarioArguments scenarioArguments) {
        return arguments(Named.of(scenarioArguments.getPath(), scenarioArguments));
    }

    private boolean scenarioContainsUISteps(final Scenario scenario) {
        return scenario.getCommands().stream()
                .anyMatch(command -> command instanceof com.knubisoft.e2e.testing.model.scenario.Ui);
    }

    private String getShortPath(final File file) {
        return file.getPath()
                .replace(TestResourceSettings.getInstance().getScenariosFolder().toString(), StringUtils.EMPTY);
    }

    @NotNull
    private List<Map<String, String>> getVariationList(final ScenarioCollector.MappingResult entry) {
        String variations = entry.scenario.getVariations();
        return new CSVParser().parseVariations(variations);
    }

    private boolean variationsExist(final ScenarioCollector.MappingResult entry) {
        return Objects.nonNull(entry.scenario) && Objects.nonNull(entry.scenario.getVariations());
    }

    private boolean uiDisabled() {
        Ui ui = GlobalTestConfigurationProvider.provide().getUi();
        return ui == null || !ui.isEnabled();
    }
}
