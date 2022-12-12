package com.knubisoft.cott.testing.framework;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestSetCollector {

    private final Map<ScenarioStepsPredicate, ScenarioToArgumentsMethod> stepsToArgumentsMethodMap;

    public TestSetCollector() {
        final List<AbstractBrowser> browsers = BrowserUtil.filterEnabledBrowsers();
        final List<MobilebrowserDevice> mobilebrowserDevices = MobileUtil.filterEnabledMobilebrowserDevices();
        final List<NativeDevice> nativeDevices = MobileUtil.filterEnabledNativeDevices();

        final Map<ScenarioStepsPredicate, ScenarioToArgumentsMethod> map = new HashMap<>();

        map.put(s -> !(s.isWeb() && s.isMobilebrowser() && s.isNatives()), this::getArgumentsWithoutUiSteps);

        map.put(s -> s.isWeb() && s.isMobilebrowser() && s.isNatives(),
                entry -> nativeDevices.stream().flatMap(nativeDevice ->
                        mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                                browsers.stream().flatMap(browser ->
                                        getArgumentsWithUiSteps(entry, browser, mobilebrowser, nativeDevice)))));
        map.put(s -> s.isWeb() && s.isNatives(),
                entry -> nativeDevices.stream().flatMap(nativeDevice ->
                        browsers.stream().flatMap(browser ->
                                getArgumentsWithUiSteps(entry, browser, null, nativeDevice))));
        map.put(s -> s.isWeb() && s.isMobilebrowser(),
                entry -> browsers.stream().flatMap(browser ->
                        mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                                getArgumentsWithUiSteps(entry, browser, mobilebrowser, null))));
        map.put(s -> s.isMobilebrowser() && s.isNatives(),
                entry -> nativeDevices.stream().flatMap(nativeDevice ->
                        mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                                getArgumentsWithUiSteps(entry, null, mobilebrowser, nativeDevice))));

        map.put(ScenarioStepReader::isWeb,
                entry -> browsers.stream().flatMap(browser ->
                        getArgumentsWithUiSteps(entry, browser, null, null)));
        map.put(ScenarioStepReader::isMobilebrowser,
                entry -> mobilebrowserDevices.stream().flatMap(mobilebrowser ->
                        getArgumentsWithUiSteps(entry, null, mobilebrowser, null)));
        map.put(ScenarioStepReader::isNatives,
                entry -> nativeDevices.stream().flatMap(nativeDevice ->
                        getArgumentsWithUiSteps(entry, null, null, nativeDevice)));

        this.stepsToArgumentsMethodMap = Collections.unmodifiableMap(map);
    }

    public Stream<Arguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<MappingResult> validScenarios = new ScenarioFilter().filterScenarios(result.get());
        return validScenarios.stream()
                .flatMap(this::createArguments);
    }

    private Stream<Arguments> createArguments(final MappingResult entry) {
        Scenario scenario = entry.scenario;
        final ScenarioStepReader scenarioSteps = new ScenarioStepReader().checkSteps(scenario);

        return stepsToArgumentsMethodMap.keySet().stream()
                .filter(stepsPredicate -> stepsPredicate.test(scenarioSteps))
                .map(stepsToArgumentsMethodMap::get)
                .findFirst().orElseThrow(() -> new DefaultFrameworkException("//todo"))
                .apply(entry);
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

    private interface ScenarioStepsPredicate extends Predicate<ScenarioStepReader> { }

    private interface ScenarioToArgumentsMethod extends Function<MappingResult, Stream<Arguments>> { }
}
