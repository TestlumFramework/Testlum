package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.parser.CSVParser;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.e2e.testing.framework.util.BrowserUtil;
import com.knubisoft.e2e.testing.model.ScenarioArguments;
import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.global_config.Ui;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestSetCollector {

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        ScenarioFilter.FiltrationResult filteredScenarios = ScenarioFilter.filterScenarios(result.get());
        if (filteredScenarios.isOnlyInvalidScenarios()) {
            return getScenarioArgumentsWithoutUIConfiguration(filteredScenarios.getInvalidScenarios()).stream();
        }
        return processFilteredScenarios(filteredScenarios);
    }

    private Stream<Arguments> processFilteredScenarios(final ScenarioFilter.FiltrationResult filteredScenarios) {
        List<Arguments> result = uiDisabled()
                ? getScenarioArgumentsWithoutUIConfiguration(filteredScenarios.getValidScenarios())
                : getScenarioArguments(filteredScenarios.getValidScenarios());
        Set<ScenarioCollector.MappingResult> invalidScenarios = filteredScenarios.getInvalidScenarios();
        if (!invalidScenarios.isEmpty()) {
            result.addAll(getScenarioArgumentsWithoutUIConfiguration(invalidScenarios));
        }
        return result.stream();
    }

    private List<Arguments> getScenarioArguments(final Set<ScenarioCollector.MappingResult> scenarios) {
        List<AbstractBrowser> webBrowserVersions = BrowserUtil.filterEnabledBrowsers();
        List<ScenarioArguments> scenarioArgumentsList = new ArrayList<>();
        scenarios.forEach(entry -> {
            if (scenarioContainsUISteps(entry.scenario)) {
                webBrowserVersions.forEach(webBrowser ->
                        addScenarioArgumentsWithUIConfiguration(entry, webBrowser, scenarioArgumentsList));
            } else {
                scenarioArgumentsList.add(getArgumentsWithoutUIConfigurations(entry));
            }
        });
        return scenarioArgumentsList.stream().map(this::convertToNamedArguments).collect(Collectors.toList());
    }

    private void addScenarioArgumentsWithUIConfiguration(final ScenarioCollector.MappingResult entry,
                                                final AbstractBrowser webBrowser,
                                                final List<ScenarioArguments> arguments) {
        BrowserSettings browserSettings = GlobalTestConfigurationProvider.getBrowserSettings();
        if (variationsExist(entry)) {
            List<Map<String, String>> variationList = getVariationList(entry);
            variationList.forEach(variation ->
                    arguments.add(getArgumentsWithUIConfigurations(entry, webBrowser, browserSettings, variation)));
        } else {
            arguments.add(getArgumentsWithUIConfigurations(entry, webBrowser, browserSettings, new HashMap<>()));
        }
    }

    private List<Arguments> getScenarioArgumentsWithoutUIConfiguration(
            final Set<ScenarioCollector.MappingResult> scenarios) {
        return scenarios.stream()
                .map(this::getArgumentsWithoutUIConfigurations)
                .map(this::convertToNamedArguments)
                .collect(Collectors.toList());
    }

    private ScenarioArguments getArgumentsWithUIConfigurations(final ScenarioCollector.MappingResult entry,
                                                               final AbstractBrowser browser,
                                                               final BrowserSettings settings,
                                                               final Map<String, String> variation) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .browser(browser)
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
        return new CSVParser().parseVariations(entry.scenario.getVariations());
    }

    private boolean variationsExist(final ScenarioCollector.MappingResult entry) {
        return Objects.nonNull(entry.scenario) && Objects.nonNull(entry.scenario.getVariations());
    }

    private boolean uiDisabled() {
        Ui ui = GlobalTestConfigurationProvider.provide().getUi();
        return ui == null || !ui.isEnabled();
    }
}
