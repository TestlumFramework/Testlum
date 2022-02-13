package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.exception.UiTestingDisableException;
import com.knubisoft.e2e.testing.framework.parser.CSVParser;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioCollector;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioFilter;
import com.knubisoft.e2e.testing.model.TestArguments;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.global_config.UiConfiguration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TestSetCollector {

    public Stream<TestArguments> collect() {
        ScenarioCollector.Result result = new ScenarioCollector().collect();
        Set<ScenarioCollector.MappingResult> filtered = ScenarioFilter.filterScenarios(result.get());
        if (uiTestingDisabled()) {
            return processScenariosOnlyForBackend(filtered);
        }
        return getTestArguments(filtered);
    }

    private Stream<TestArguments> getTestArguments(final Set<ScenarioCollector.MappingResult> scenarios) {
        Set<ScenarioCollector.MappingResult> uiScenarios =
                ScenarioFilter.filterScenarioByType(ScenarioFilter.ScenarioType.UI, scenarios);
        Set<ScenarioCollector.MappingResult> backendScenarios =
                ScenarioFilter.filterScenarioByType(ScenarioFilter.ScenarioType.BACKEND, scenarios);
        List<TestArguments> testArguments = new ArrayList<>();
        testArguments.addAll(getTestArgumentsForUiScenarios(uiScenarios));
        testArguments.addAll(getTestArgumentsForBackendScenarios(backendScenarios));
        return testArguments.stream();
    }

    private Stream<TestArguments> processScenariosOnlyForBackend(final Set<ScenarioCollector.MappingResult> scenarios) {
        Set<ScenarioCollector.MappingResult> backendScenarios
                = ScenarioFilter.filterScenarioByType(ScenarioFilter.ScenarioType.BACKEND, scenarios);
        if (backendScenarios.isEmpty()) {
            throw new UiTestingDisableException();
        }
        return getTestArgumentsForBackendScenarios(backendScenarios).stream();
    }

    private List<TestArguments> getTestArgumentsForBackendScenarios(
            final Set<ScenarioCollector.MappingResult> backendScenarios) {
        return backendScenarios.stream()
                .map(this::getBackendTestArguments)
                .collect(Collectors.toList());
    }

    private List<TestArguments> getTestArgumentsForUiScenarios(
            final Set<ScenarioCollector.MappingResult> uiScenarios) {
        List<String> browserVersions = GlobalTestConfigurationProvider.getBrowserSettings().getVersions().getVersion();
        if (browserVersions.size() == 1) {
            List<TestArguments> uiTestArguments = new ArrayList<>();
            uiScenarios.forEach(scenario -> addUiTestArguments(scenario, browserVersions.get(0), uiTestArguments));
            return uiTestArguments;
        } else {
            return copyTestArgumentsForEachBrowserVersion(uiScenarios, browserVersions);
        }
    }

    private List<TestArguments> copyTestArgumentsForEachBrowserVersion(
            final Set<ScenarioCollector.MappingResult> scenarios,
            final List<String> browserVersion) {
        List<TestArguments> uiTestArgumentsForEachVersion = new ArrayList<>();
        scenarios.forEach(scenario -> {
            browserVersion.forEach(version -> addUiTestArguments(scenario, version, uiTestArgumentsForEachVersion));
        });
        return uiTestArgumentsForEachVersion;
    }

    private void addUiTestArguments(final ScenarioCollector.MappingResult entry,
                                    final String browserVersion,
                                    final List<TestArguments> uiTestArguments) {
        BrowserSettings browserSettings = GlobalTestConfigurationProvider.getBrowserSettings();
        if (!checkIfScenarioWithoutVariations(entry)) {
            List<Map<String, String>> variationList = getVariationList(entry);
            variationList.forEach(variation ->
                    uiTestArguments.add(getUiTestArguments(entry, browserVersion, browserSettings, variation)));
        } else {
            uiTestArguments.add(getUiTestArguments(entry, browserVersion, browserSettings, new HashMap<>()));
        }
    }

    private TestArguments getUiTestArguments(final ScenarioCollector.MappingResult entry, final String browserVersion,
                                             final BrowserSettings settings, final Map<String, String> variation) {
        return TestArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .mappingResult(entry)
                .browserVersion(browserVersion)
                .browserSettings(settings)
                .variation(variation)
                .containsUiSteps(true)
                .build();
    }

    private TestArguments getBackendTestArguments(final ScenarioCollector.MappingResult entry) {
        return TestArguments.builder()
                .path(getShortPath(entry.file))
                .file(entry.file)
                .mappingResult(entry)
                .browserVersion(null)
                .browserSettings(null)
                .variation(new HashMap<>())
                .containsUiSteps(false)
                .build();
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

    private boolean checkIfScenarioWithoutVariations(final ScenarioCollector.MappingResult entry) {
        return !Objects.nonNull(entry.scenario) || Objects.isNull(entry.scenario.getVariations());
    }

    private boolean uiTestingDisabled() {
        UiConfiguration uiConfiguration = GlobalTestConfigurationProvider.provide().getUiConfiguration();
        return uiConfiguration == null || !uiConfiguration.isEnabled();
    }
}
