package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioCollector {

    private final ScenarioValidator scenarioValidator;
    private final XMLParsers xmlParsers;
    private final TestResourceSettings testResourceSettings;
    private final GlobalVariationsProvider globalVariationsProvider;
    private final AuthCommandExpander authCommandExpander;

    public Result collect() {
        List<File> scenarios = new ArrayList<>();
        if (testResourceSettings.getScenarioScopeFolder().isPresent()) {
            walk(testResourceSettings.getScenarioScopeFolder().get(), scenarios);
        } else {
            walk(testResourceSettings.getTestResourcesFolder(), scenarios);
        }
        Result result = new Result();
        for (File each : scenarios) {
            applyXml(each, result);
        }
        return result;
    }

    private void walk(final File root, final List<File> scenarios) {
        File[] listFiles = root.listFiles();
        if (Objects.nonNull(listFiles)) {
            for (File file : listFiles) {
                processEachFile(scenarios, file);
            }
        }
    }

    private void processEachFile(final List<File> scenarios, final File file) {
        if (file.isDirectory()) {
            walk(file, scenarios);
        } else {
            if (file.getName().equals(TestResourceSettings.SCENARIO_FILENAME)) {
                scenarios.add(file);
            }
        }
    }

    private void applyXml(final File xmlFile, final Result result) {
        Scenario scenario = null;
        try {
            scenario = xmlParsers.forScenario().process(xmlFile);
            processScenarioVariations(xmlFile, scenario);
            updateScenario(scenario);
            scenarioValidator.validate(scenario, xmlFile);
            result.add(new MappingResult(xmlFile, scenario, null));
        } catch (Exception e) {
            result.add(new MappingResult(xmlFile, scenario, e));
        }
    }

    private void processScenarioVariations(final File xmlFile, final Scenario scenario) {
        if (Objects.nonNull(scenario.getSettings())
                && Objects.nonNull(scenario.getSettings().getVariations())) {
            globalVariationsProvider.process(scenario, xmlFile);
        }
    }

    private void updateScenario(final Scenario scenario) {
        List<AbstractCommand> updatedCommands = authCommandExpander.expand(scenario.getCommands());
        scenario.getCommands().clear();
        scenario.getCommands().addAll(updatedCommands);
    }

    public static class Result extends ArrayList<MappingResult> {

    }

    @RequiredArgsConstructor
    public static class MappingResult {
        public final File file;
        public final Scenario scenario;
        public final Exception exception;
    }
}
