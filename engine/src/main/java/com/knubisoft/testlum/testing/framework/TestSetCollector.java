package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.util.ScenarioStepReader;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsImpl.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Service
public class TestSetCollector {

    private final List<String> browsers;
    private final List<String> mobileBrowsers;
    private final List<String> nativeDevices;

    public TestSetCollector() {
        this.browsers = BrowserUtil.filterDefaultEnabledBrowsers().stream()
                .map(AbstractBrowser::getAlias).collect(Collectors.toList());
        this.mobileBrowsers = MobileUtil.filterDefaultEnabledMobileBrowserDevices().stream()
                .map(MobilebrowserDevice::getAlias).collect(Collectors.toList());
        this.nativeDevices = MobileUtil.filterDefaultEnabledNativeDevices().stream()
                .map(NativeDevice::getAlias).collect(Collectors.toList());
    }

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        List<MappingResult> validScenarios = new ScenarioFilter().filterScenarios(result);
        return validScenarios.stream().flatMap(this::createArguments);
    }

    //CHECKSTYLE:OFF
    private Stream<Arguments> createArguments(final MappingResult entry) {
        final ScenarioStepReader s = new ScenarioStepReader(entry.scenario);

        if (s.isWeb()) {
            if (s.isMobileBrowser()) {
                if (s.isNatives()) {
                    return nativeDevices.stream().flatMap(nativeDevice ->
                            mobileBrowsers.stream().flatMap(mobileBrowser ->
                                    browsers.stream().flatMap(browser ->
                                            getArgumentsWithUiSteps(entry, browser, mobileBrowser, nativeDevice))));
                }
                return browsers.stream().flatMap(browser ->
                        mobileBrowsers.stream().flatMap(mobileBrowser ->
                                getArgumentsWithUiSteps(entry, browser, mobileBrowser, null)));
            }
            if (s.isNatives()) {
                return nativeDevices.stream().flatMap(nativeDevice ->
                        browsers.stream().flatMap(browser ->
                                getArgumentsWithUiSteps(entry, browser, null, nativeDevice)));
            }
            return browsers.stream().flatMap(browser ->
                    getArgumentsWithUiSteps(entry, browser, null, null));
        }
        if (s.isMobileBrowser()) {
            if (s.isNatives()) {
                return nativeDevices.stream().flatMap(nativeDevice ->
                        mobileBrowsers.stream().flatMap(mobileBrowser ->
                                getArgumentsWithUiSteps(entry, null, mobileBrowser, nativeDevice)));
            }
            return mobileBrowsers.stream().flatMap(mobileBrowser ->
                    getArgumentsWithUiSteps(entry, null, mobileBrowser, null));
        }
        if (s.isNatives()) {
            return nativeDevices.stream().flatMap(nativeDevice ->
                    getArgumentsWithUiSteps(entry, null, null, nativeDevice));
        }
        return getArgumentsWithoutUiSteps(entry);
    }
    //CHECKSTYLE:ON

    private Stream<Arguments> getArgumentsWithoutUiSteps(final MappingResult entry) {
        if (variationsExist(entry)) {
            return getVariationList(entry).stream().map(variations -> getArgumentsWithoutUiSteps(entry, variations));
        }
        return Stream.of(getArgumentsWithoutUiSteps(entry, new HashMap<>()));
    }

    private Arguments getArgumentsWithoutUiSteps(final MappingResult entry, final Map<String, String> variations) {
        ScenarioArguments scenarioArguments = buildScenarioArguments(entry, variations);
        return convertToNamedArguments(scenarioArguments);
    }

    private Stream<Arguments> getArgumentsWithUiSteps(final MappingResult entry,
                                                      final String browserAlias,
                                                      final String mobileBrowserAlias,
                                                      final String nativeAlias) {
        if (variationsExist(entry)) {
            return getVariationList(entry).stream().map(variations ->
                    getArgumentsWithUiSteps(entry, browserAlias, mobileBrowserAlias, nativeAlias, variations));
        } else {
            return Stream.of(
                    getArgumentsWithUiSteps(entry, browserAlias, mobileBrowserAlias, nativeAlias, new HashMap<>()));
        }
    }

    private Arguments getArgumentsWithUiSteps(final MappingResult entry,
                                              final String browserAlias,
                                              final String mobileBrowserAlias,
                                              final String nativeAlias,
                                              final Map<String, String> variations) {
        ScenarioArguments scenarioArguments = buildScenarioArguments(
                entry, browserAlias, mobileBrowserAlias, nativeAlias, variations);
        return convertToNamedArguments(scenarioArguments);
    }

    private ScenarioArguments buildScenarioArguments(final MappingResult entry,
                                                     final String browserAlias,
                                                     final String mobileBrowserAlias,
                                                     final String nativeAlias,
                                                     final Map<String, String> variations) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .browser(browserAlias)
                .mobileBrowserDevice(mobileBrowserAlias)
                .nativeDevice(nativeAlias)
                .variations(variations)
                .containsUiSteps(true)
                .build();
    }

    private ScenarioArguments buildScenarioArguments(final MappingResult entry,
                                                     final Map<String, String> variations) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(entry.scenario)
                .exception(entry.exception)
                .variations(variations)
                .build();
    }

    private Arguments convertToNamedArguments(final ScenarioArguments scenarioArguments) {
        return arguments(Named.of(scenarioArguments.getPath(), scenarioArguments));
    }

    private String getShortPath(final File file) {
        File scenariosFolder = TestResourceSettings.getInstance().getScenariosFolder();
        return file.getPath().replace(scenariosFolder.toString(), StringUtils.EMPTY);
    }

    private boolean variationsExist(final MappingResult entry) {
        return nonNull(entry.scenario) && StringUtils.isNotBlank(entry.scenario.getSettings().getVariations());
    }

    private List<Map<String, String>> getVariationList(final MappingResult entry) {
        return GlobalVariationsProvider.getVariations(entry.scenario.getSettings().getVariations());
    }
}
