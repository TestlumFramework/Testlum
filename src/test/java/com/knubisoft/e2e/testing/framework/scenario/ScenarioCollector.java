package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.parser.XMLParsers;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.Include;
import com.knubisoft.e2e.testing.model.scenario.Logout;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@Slf4j
public class ScenarioCollector {

    private final File rootTestResources;
    private final Pattern pattern;
    private final FileSearcher fileSearcher;
    private final ScenarioValidator scenarioValidator;

    public ScenarioCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.rootTestResources = resourceSettings.getTestResourcesFolder();
        this.pattern = Pattern.compile(GlobalTestConfigurationProvider.provide().getFilterDirectoryPattern());
        this.fileSearcher = new FileSearcher(rootTestResources, false);
        this.scenarioValidator = new ScenarioValidator(fileSearcher);
    }

    public Result collect() {
        List<File> scenarios = new ArrayList<>();
        walk(rootTestResources, scenarios);
        Result result = new Result();
        for (File each : scenarios) {
            applyXml(each, result);
        }
        return result;
    }

    private void applyXml(final File xmlFile, final Result result) {
        try {
            Scenario scenario = convertXmlToScenario(xmlFile);
            result.add(new MappingResult(xmlFile, scenario, null));
        } catch (Exception e) {
            result.add(new MappingResult(xmlFile, null, e));
        }
    }

    private Scenario convertXmlToScenario(final File xmlFile) {
        Scenario scenario = XMLParsers.forScenario().process(xmlFile, scenarioValidator);
        updateScenario(scenario);
        return scenario;
    }

    private void updateScenario(final Scenario scenario) {
        List<AbstractCommand> updatedCommands = updateCommands(scenario.getCommands());
        updatedCommands = updateCommands(updatedCommands);
        scenario.getCommands().clear();
        scenario.getCommands().addAll(updatedCommands);
    }

    private List<AbstractCommand> updateCommands(final List<AbstractCommand> commands) {
        List<AbstractCommand> updatedCommands = new ArrayList<>();
        for (AbstractCommand command : commands) {
            addAbstractCommand(updatedCommands, command);
        }
        return updatedCommands;
    }

    private void addAbstractCommand(final List<AbstractCommand> updatedCommand,
                                    final AbstractCommand command) {
        if (command instanceof Auth) {
            addAuthCommands(updatedCommand, (Auth) command);
        } else if (command instanceof Include) {
            addIncludeCommands(updatedCommand, command);
        } else {
            updatedCommand.add(command);
        }
    }

    private void addIncludeCommands(final List<AbstractCommand> updatedCommands, final AbstractCommand command) {
        Include include = (Include) command;
        Scenario includedScenario = findIncludedScenarioAndParse(include);
        updateScenario(includedScenario);
        updatedCommands.addAll(includedScenario.getCommands());
    }

    private Scenario findIncludedScenarioAndParse(final Include include) {
        File scenariosFolder = TestResourceSettings.getInstance().getScenariosFolder();
        File includedScenarioFolder = new File(scenariosFolder,
                include.getScenario());
        File file = fileSearcher.search(includedScenarioFolder, TestResourceSettings.SCENARIO_FILENAME);
        return XMLParsers.forScenario().process(file, scenarioValidator);
    }

    private void addAuthCommands(final List<AbstractCommand> updatedCommand,
                                 final Auth authCommand) {
        Auth auth = new Auth();
        auth.setComment(authCommand.getComment());
        auth.setCredentials(authCommand.getCredentials());
        updatedCommand.add(auth);
        updatedCommand.addAll(authCommand.getCommands());
        updatedCommand.add(new Logout());
    }

    private void walk(final File root, final List<File> scenarios) {
        File[] listFiles = root.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                processEachFile(scenarios, file);
            }
        }
    }

    private void processEachFile(final List<File> scenarios, final File file) {
        if (file.isDirectory()) {
            walk(file, scenarios);
        } else {
            if (file.getName().equals(TestResourceSettings.SCENARIO_FILENAME) && matchesPathPattern(file)) {
                scenarios.add(file);
            }
        }
    }

    private boolean matchesPathPattern(final File file) {
        String pathFromRoot = file.getPath().replace(rootTestResources.getPath(), StringUtils.EMPTY);
        return pattern.matcher(pathFromRoot).matches();
    }

    public static class Result {

        private static final TreeSet<MappingResult> SET_MAP = new TreeSet<>(new Comparator<MappingResult>() {
            @Override
            public int compare(final MappingResult o1, final MappingResult o2) {
                return getReadonlyValue(o1) ? -1 : 1;
            }

            private boolean getReadonlyValue(final MappingResult result) {
                return result.scenario != null && (result.scenario.getTags().isReadonly());
            }
        });

        public void add(final MappingResult mappingResult) {
            SET_MAP.add(mappingResult);
        }

        public Set<MappingResult> get() {
            return SET_MAP;
        }
    }

    @RequiredArgsConstructor
    public static class MappingResult {
        public final File file;
        public final Scenario scenario;
        public final Exception exception;
    }
}
