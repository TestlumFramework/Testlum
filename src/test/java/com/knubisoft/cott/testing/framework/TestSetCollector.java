package com.knubisoft.cott.testing.framework;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.parser.CSVParser;
import com.knubisoft.cott.testing.framework.scenario.FiltrationResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.cott.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.cott.testing.framework.util.BrowserUtil;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.Ui;
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

import static com.knubisoft.cott.testing.framework.util.LogMessage.UI_DISABLED_ERROR;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestSetCollector {

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        FiltrationResult filtrationResult = ScenarioFilter.filterScenarios(result.get());
        if (filtrationResult.isOnlyScenariosWithoutUiSteps()) {
            return filtrationResult.getScenariosWithoutUiSteps()
                    .stream().map(this::getArgumentsWithoutUiSteps).map(this::convertToNamedArguments);
        }
        checkIfUiEnabled();
        return getScenarioArguments(filtrationResult.getScenariosWithUiSteps(),
                filtrationResult.getScenariosWithoutUiSteps());
    }

    private Stream<Arguments> getScenarioArguments(final Set<ScenarioCollector.MappingResult> scenariosWithUiSteps,
                                                 final Set<ScenarioCollector.MappingResult> scenariosWithoutUiSteps) {
        List<AbstractBrowser> browsers = BrowserUtil.filterEnabledBrowsers();
        List<ScenarioArguments> scenarioArguments = new ArrayList<>();
        scenariosWithUiSteps.forEach(scenario ->
                browsers.forEach(browser -> addScenarioArgumentsWithUiSteps(scenario, browser, scenarioArguments)));
        scenariosWithoutUiSteps.forEach(scenario -> scenarioArguments.add(getArgumentsWithoutUiSteps(scenario)));
        return scenarioArguments.stream().map(this::convertToNamedArguments);
    }

    private void addScenarioArgumentsWithUiSteps(final ScenarioCollector.MappingResult entry,
                                                final AbstractBrowser webBrowser,
                                                final List<ScenarioArguments> arguments) {
        if (variationsExist(entry)) {
            List<Map<String, String>> variationList = getVariationList(entry);
            variationList.forEach(variation ->
                    arguments.add(getArgumentsWithUiSteps(entry, webBrowser, variation)));
        } else {
            arguments.add(getArgumentsWithUiSteps(entry, webBrowser, new HashMap<>()));
        }
    }

    private ScenarioArguments getArgumentsWithUiSteps(final ScenarioCollector.MappingResult entry,
                                                               final AbstractBrowser browser,
                                                               final Map<String, String> variation) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .browser(browser)
                .variation(variation)
                .containsUiSteps(true)
                .build();
    }

    private ScenarioArguments getArgumentsWithoutUiSteps(final ScenarioCollector.MappingResult entry) {
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

    private void checkIfUiEnabled() {
        Ui ui = GlobalTestConfigurationProvider.provide().getUi();
        if (ui == null || !ui.isEnabled()) {
            throw new DefaultFrameworkException(UI_DISABLED_ERROR);
        }
    }
}
