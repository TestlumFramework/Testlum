package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.util.ScenarioStepReader;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Component
public class TestSetCollector {

    private final List<String> browsers;
    private final List<String> mobileBrowsers;
    private final List<String> nativeDevices;
    private final ScenarioCollector scenarioCollector;
    private final ScenarioFilter scenarioFilter;
    private final TestResourceSettings testResourceSettings;
    private final GlobalVariationsProvider globalVariationsProvider;
    private final List<Environment> environments;
    private final JacksonService jacksonService;

    public TestSetCollector(final ScenarioCollector scenarioCollector,
                            final ScenarioFilter scenarioFilter,
                            final BrowserUtil browserUtil,
                            final MobileUtil mobileUtil,
                            final TestResourceSettings testResourceSettings,
                            final GlobalVariationsProvider globalVariationsProvider,
                            final List<Environment> environments,
                            final JacksonService jacksonService) {
        this.scenarioCollector = scenarioCollector;
        this.scenarioFilter = scenarioFilter;
        this.browsers = browserUtil.filterDefaultEnabledBrowsers().stream()
                .map(AbstractBrowser::getAlias).toList();
        this.mobileBrowsers = mobileUtil.filterDefaultEnabledMobileBrowserDevices().stream()
                .map(MobilebrowserDevice::getAlias).toList();
        this.nativeDevices = mobileUtil.filterDefaultEnabledNativeDevices().stream()
                .map(NativeDevice::getAlias).toList();
        this.testResourceSettings = testResourceSettings;
        this.globalVariationsProvider = globalVariationsProvider;
        this.environments = environments;
        this.jacksonService = jacksonService;
    }

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = scenarioCollector.collect();
        List<MappingResult> validScenarios = scenarioFilter.filterScenarios(result);
        List<String> executionEnvironments = resolveExecutionEnvironments();
        return validScenarios.stream()
                .flatMap(this::createArguments)
                .flatMap(arguments -> expandByEnvironment(arguments, executionEnvironments));
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
                .scenario(deepCopyScenario(entry.scenario))
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
                .scenario(deepCopyScenario(entry.scenario))
                .exception(entry.exception)
                .variations(variations)
                .build();
    }

    private Arguments convertToNamedArguments(final ScenarioArguments scenarioArguments) {
        return arguments(Named.of(scenarioArguments.getPath(), scenarioArguments));
    }

    private Stream<Arguments> expandByEnvironment(final Arguments argument, final List<String> executionEnvironments) {
        Object[] values = argument.get();
        if (values.length == 0 || !(values[0] instanceof Named<?> named)) {
            return Stream.of(argument);
        }
        Object payload = named.getPayload();
        if (!(payload instanceof ScenarioArguments base)) {
            return Stream.of(argument);
        }
        return executionEnvironments.stream()
                .map(environment -> cloneForEnvironment(base, environment))
                .map(scenarioArguments -> arguments(Named.of(
                        scenarioArguments.getPath() + " [" + scenarioArguments.getEnvironment() + "]",
                        scenarioArguments)));
    }

    private List<String> resolveExecutionEnvironments() {
        if (environments == null || environments.isEmpty()) {
            throw new DefaultFrameworkException("No enabled environments found for test execution");
        }
        return environments.stream().map(Environment::getFolder).toList();
    }

    private ScenarioArguments cloneForEnvironment(final ScenarioArguments base, final String environment) {
        ScenarioArguments cloned = ScenarioArguments.builder()
                .path(base.getPath())
                .file(base.getFile())
                .scenario(base.getScenario())
                .exception(base.getException())
                .browser(base.getBrowser())
                .mobileBrowserDevice(base.getMobileBrowserDevice())
                .nativeDevice(base.getNativeDevice())
                .variations(base.getVariations() == null ? null : new HashMap<>(base.getVariations()))
                .containsUiSteps(base.isContainsUiSteps())
                .build();
        cloned.setEnvironment(environment);
        return cloned;
    }

    private Scenario deepCopyScenario(final Scenario src) {
        String json = jacksonService.writeValueToCopiedString(src);
        return jacksonService.readCopiedValue(json, Scenario.class);
    }

    private String getShortPath(final File file) {
        File scenariosFolder = testResourceSettings.getScenariosFolder();
        return file.getPath().replace(scenariosFolder.toString(), StringUtils.EMPTY);
    }

    private boolean variationsExist(final MappingResult entry) {
        return nonNull(entry.scenario) && StringUtils.isNotBlank(entry.scenario.getSettings().getVariations());
    }

    private List<Map<String, String>> getVariationList(final MappingResult entry) {
        return globalVariationsProvider.getVariations(entry.scenario.getSettings().getVariations());
    }
}
