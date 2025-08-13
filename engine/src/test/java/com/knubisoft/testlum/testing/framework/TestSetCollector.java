package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioCollector.MappingResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.util.ScenarioStepReader;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsImpl.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Service
public class TestSetCollector {

    private final List<String> browsers;
    private final List<String> mobilebrowsers;
    private final List<String> nativeDevices;

    public TestSetCollector() {
        browsers = BrowserUtil.filterDefaultEnabledBrowsers().stream()
                .map(AbstractBrowser::getAlias).collect(Collectors.toList());
        mobilebrowsers = MobileUtil.filterDefaultEnabledMobilebrowserDevices().stream()
                .map(MobilebrowserDevice::getAlias).collect(Collectors.toList());
        nativeDevices = MobileUtil.filterDefaultEnabledNativeDevices().stream()
                .map(NativeDevice::getAlias).collect(Collectors.toList());
    }


    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<MappingResult> validScenarios = new ScenarioFilter().filterScenarios(result.get());
        return validScenarios.stream()
                .flatMap(this::createArguments);
    }

    //CHECKSTYLE:OFF
    private Stream<Arguments> createArguments(final MappingResult entry) {
        Scenario scenario = entry.scenario;
        final ScenarioStepReader s = new ScenarioStepReader().checkSteps(scenario);

        if (s.isWeb()) {
            if (s.isMobilebrowser()) {
                if (s.isNatives()) {
                    return nativeDevices.stream().flatMap(nativeDevice ->
                            mobilebrowsers.stream().flatMap(mobilebrowser ->
                                    browsers.stream().flatMap(browser ->
                                            getArgumentsWithUiSteps(entry, browser, mobilebrowser, nativeDevice))));
                }
                return browsers.stream().flatMap(browser ->
                        mobilebrowsers.stream().flatMap(mobilebrowser ->
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
                        mobilebrowsers.stream().flatMap(mobilebrowser ->
                                getArgumentsWithUiSteps(entry, null, mobilebrowser, nativeDevice)));
            }
            return mobilebrowsers.stream().flatMap(mobilebrowser ->
                    getArgumentsWithUiSteps(entry, null, mobilebrowser, null));
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
                                                      final String mobilebrowserAlias,
                                                      final String nativeAlias) {
        if (variationsExist(entry)) {
            return getVariationList(entry).stream().map(variations ->
                    getArgumentsWithUiSteps(entry, browserAlias, mobilebrowserAlias, nativeAlias, variations));
        } else {
            return Stream.of(
                    getArgumentsWithUiSteps(entry, browserAlias, mobilebrowserAlias, nativeAlias, new HashMap<>()));
        }
    }

    private Arguments getArgumentsWithUiSteps(final MappingResult entry,
                                              final String browserAlias,
                                              final String mobilebrowserAlias,
                                              final String nativeAlias,
                                              final Map<String, String> variations) {
        ScenarioArguments scenarioArguments = buildScenarioArguments(
                entry, browserAlias, mobilebrowserAlias, nativeAlias, variations);
        return convertToNamedArguments(scenarioArguments);
    }

    private ScenarioArguments buildScenarioArguments(final MappingResult entry,
                                                     final String browserAlias,
                                                     final String mobilebrowserAlias,
                                                     final String nativeAlias,
                                                     final Map<String, String> variations) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(deepCopyScenario(entry.scenario))
                .exception(entry.exception)
                .browser(browserAlias)
                .mobilebrowserDevice(mobilebrowserAlias)
                .nativeDevice(nativeAlias)
                .variations(variations)
                .containsUiSteps(true)
                .build();
    }

    private ScenarioArguments buildScenarioArguments(final MappingResult entry, final Map<String, String> variations) {
        return ScenarioArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .scenario(deepCopyScenario(entry.scenario))
                .exception(entry.exception)
                .variations(variations)
                .build();
    }

    private Scenario deepCopyScenario(Scenario src) {
        String json = JacksonMapperUtil.writeValueToCopiedString(src);
        return JacksonMapperUtil.readCopiedValue(json, Scenario.class);
    }

    private Arguments convertToNamedArguments(final ScenarioArguments scenarioArguments) {
        return arguments(Named.of(scenarioArguments.getPath(), scenarioArguments));
    }

    private String getShortPath(final File file) {
        return file.getPath()
                .replace(TestResourceSettings.getInstance().getScenariosFolder().toString(), StringUtils.EMPTY);
    }

    private boolean variationsExist(final MappingResult entry) {
        return nonNull(entry.scenario) && StringUtils.isNotBlank(entry.scenario.getSettings().getVariations());
    }

    private List<Map<String, String>> getVariationList(final MappingResult entry) {
        return GlobalVariationsProvider.getVariations(entry.scenario.getSettings().getVariations());
    }
}
