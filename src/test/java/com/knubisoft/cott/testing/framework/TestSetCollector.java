package com.knubisoft.cott.testing.framework;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.parser.CSVParser;
import com.knubisoft.cott.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.cott.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.cott.testing.framework.util.BrowserUtil;
import com.knubisoft.cott.testing.framework.util.MobileUtil;
import com.knubisoft.cott.testing.framework.util.ScenarioStepReader;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import com.knubisoft.cott.testing.model.scenario.Web;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestSetCollector {

    private final List<AbstractBrowser> browsers = BrowserUtil.filterEnabledBrowsers();
    private final List<MobilebrowserDevice> mobilebrowserDevices = MobileUtil.filterEnabledMobilebrowserDevices();
    private final List<NativeDevice> nativeDevices = MobileUtil.filterEnabledNativeDevices();

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<MappingResult> validScenarios = new ScenarioFilter().filterScenarios(result.get());
        if (GlobalTestConfigurationProvider.isWebParallel()) {
            return validScenarios.stream()
                    .filter(r -> !containsOnlyWeb(r.scenario)).flatMap(this::createArguments);
        }
        return validScenarios.stream()
                .flatMap(this::createArguments);
    }

    public Stream<Arguments> onlyWeb() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<MappingResult> validScenarios = new ScenarioFilter().filterScenarios(result.get());
        return validScenarios.stream().filter(s -> containsOnlyWeb(s.scenario))
                .flatMap(this::getArgumentsForParallelWeb);
    }

    //CHECKSTYLE:OFF
    private Stream<Arguments> createArguments(final MappingResult entry) {
        Scenario scenario = entry.scenario;
        final ScenarioStepReader s = new ScenarioStepReader().checkSteps(scenario);

        if (s.isWeb()) {
            if (s.isMobilebrowser()) {
                if (s.isNatives()) {
                    return nativeDevices.stream().flatMap(nativeDevice ->
                            mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                                    browsers.stream().flatMap(browser ->
                                            getArgumentsWithUiSteps(entry, browser, mobilebrowser, nativeDevice))));
                }
                return browsers.stream().flatMap(browser ->
                        mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                                getArgumentsWithUiSteps(entry, browser, mobilebrowser, null)));
            }
            if (s.isNatives()) {
                return nativeDevices.stream().flatMap(nativeDevice ->
                        browsers.stream().flatMap(browser ->
                                getArgumentsWithUiSteps(entry, browser, null, nativeDevice)));
            }
            return browsers.stream().flatMap(browser ->
                    getArgumentsWithUiSteps(entry, browser, null, null));
        }
        if (s.isMobilebrowser()) {
            if (s.isNatives()) {
                return nativeDevices.stream().flatMap(nativeDevice ->
                        mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                                getArgumentsWithUiSteps(entry, null, mobilebrowser, nativeDevice)));
            }
            return mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                    getArgumentsWithUiSteps(entry, null, mobilebrowser, null));
        }
        if (s.isNatives()) {
            return nativeDevices.stream().flatMap(nativeDevice ->
                    getArgumentsWithUiSteps(entry, null, null, nativeDevice));
        }
        return getArgumentsWithoutUiSteps(entry);
    }
    //CHECKSTYLE:ON

    private Stream<Arguments> getArgumentsForParallelWeb(final MappingResult entry) {
        return getArgumentsWithUiSteps(entry, null, null, null);
    }
    private Stream<Arguments> getArgumentsWithoutUiSteps(final MappingResult entry) {
        ScenarioArguments scenarioArguments = buildScenarioArguments(entry);
        return Stream.of(convertToNamedArguments(scenarioArguments));
    }

    private Stream<Arguments> getArgumentsWithUiSteps(final MappingResult entry,
                                                      final AbstractBrowser webBrowser,
                                                      final MobilebrowserDevice mobilebrowserDevice,
                                                      final NativeDevice nativeDevice) {
        if (variationsExist(entry)) {
            return getVariationList(entry).stream().map(variation ->
                    getArgumentsWithUiSteps(entry, webBrowser, mobilebrowserDevice, nativeDevice, variation));
        } else {
            return Stream.of(
                    getArgumentsWithUiSteps(entry, webBrowser, mobilebrowserDevice, nativeDevice, new HashMap<>()));
        }
    }

    private Arguments getArgumentsWithUiSteps(final MappingResult entry,
                                              final AbstractBrowser webBrowser,
                                              final MobilebrowserDevice mobilebrowserDevice,
                                              final NativeDevice nativeDevice,
                                              final Map<String, String> variation) {
        ScenarioArguments scenarioArguments = buildScenarioArguments(
                entry, webBrowser, mobilebrowserDevice, nativeDevice, variation);
        return convertToNamedArguments(scenarioArguments);
    }

    private ScenarioArguments buildScenarioArguments(final MappingResult entry,
                                                     final AbstractBrowser browser,
                                                     final MobilebrowserDevice mobilebrowserDevice,
                                                     final NativeDevice nativeDevice,
                                                     final Map<String, String> variation) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .browser(browser)
                .mobilebrowserDevice(mobilebrowserDevice)
                .nativeDevice(nativeDevice)
                .variation(variation)
                .containsUiSteps(true)
                .build();
    }

    private ScenarioArguments buildScenarioArguments(final MappingResult entry) {
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

    private List<Map<String, String>> getVariationList(final MappingResult entry) {
        return new CSVParser().parseVariations(entry.scenario.getVariations());
    }

    private boolean variationsExist(final MappingResult entry) {
        return Objects.nonNull(entry.scenario) && Objects.nonNull(entry.scenario.getVariations());
    }

    private boolean containsOnlyWeb(final Scenario scenario) {
        return scenario.getCommands().stream().allMatch(abstractCommand -> abstractCommand instanceof Web);
    }
}
