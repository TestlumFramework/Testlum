package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import com.knubisoft.testlum.testing.model.scenario.Include;
import com.knubisoft.testlum.testing.model.scenario.Logout;
import com.knubisoft.testlum.testing.model.scenario.Repeat;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_NOT_FOUND;
import static java.util.Objects.nonNull;

@Slf4j
public class ScenarioCollector {

    private final File rootTestResources;
    private final ScenarioValidator scenarioValidator;

    public ScenarioCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.rootTestResources = resourceSettings.getTestResourcesFolder();
        this.scenarioValidator = new ScenarioValidator();
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

    private void walk(final File root, final List<File> scenarios) {
        File[] listFiles = root.listFiles();
        if (nonNull(listFiles)) {
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
        try {
            Scenario scenario = convertXmlToScenario(xmlFile);
            result.add(new MappingResult(xmlFile, scenario, null));
        } catch (Exception e) {
            result.add(new MappingResult(xmlFile, null, e));
        }
    }

    private Scenario convertXmlToScenario(final File xmlFile) {
        Scenario scenario = XMLParsers.forScenario().process(xmlFile);
        scenarioValidator.validate(updateScenario(scenario), xmlFile);
        return scenario;
    }

    private Scenario updateScenario(final Scenario scenario) {
        List<AbstractCommand> updatedCommands = updateCommands(scenario.getCommands());
        scenario.getCommands().clear();
        scenario.getCommands().addAll(updatedCommands);
        return scenario;
    }

    private List<AbstractCommand> updateCommands(final List<AbstractCommand> commands) {
        List<AbstractCommand> updatedCommands = new ArrayList<>();
        for (AbstractCommand command : commands) {
            addAbstractCommand(updatedCommands, command);
        }
        return updatedCommands;
    }

    private void addAbstractCommand(final List<AbstractCommand> updatedCommand, final AbstractCommand command) {
        if (command instanceof Auth) {
            addAuthCommands(updatedCommand, (Auth) command);
        } else if (command instanceof Include) {
            addIncludeCommands(updatedCommand, command);
        } else if (command instanceof Repeat) {
            addRepeatCommands(updatedCommand, (Repeat) command);
        } else {
            updatedCommand.add(command);
        }
    }

    private void addAuthCommands(final List<AbstractCommand> updatedCommand, final Auth authCommand) {
        Auth auth = new Auth();
        auth.setComment(authCommand.getComment());
        auth.setCredentials(authCommand.getCredentials());
        auth.setApiAlias(authCommand.getApiAlias());
        auth.setLoginEndpoint(authCommand.getLoginEndpoint());
        updatedCommand.add(auth);
        updatedCommand.addAll(authCommand.getCommands());
        if (isAutoLogout(authCommand.getApiAlias())) {
            Logout logout = new Logout();
            logout.setAlias(authCommand.getApiAlias());
            updatedCommand.add(logout);
        }
    }

    private boolean isAutoLogout(final String alias) {
        //todo move to interpreter
        List<Api> apiList = GlobalTestConfigurationProvider.getDefaultIntegrations().getApis().getApi();
        Api apiIntegration = IntegrationsUtil.findApiForAlias(apiList, alias);
        if (nonNull(apiIntegration.getAuth())) {
            return apiIntegration.getAuth().isAutoLogout();
        }
        throw new DefaultFrameworkException(AUTH_NOT_FOUND, apiIntegration.getAlias());
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
        File file = FileSearcher.searchFileFromDir(includedScenarioFolder, TestResourceSettings.SCENARIO_FILENAME);
        return XMLParsers.forScenario().process(file, scenarioValidator);
    }

    private void addRepeatCommands(final List<AbstractCommand> updatedCommand, final Repeat repeatCommand) {
        int times = repeatCommand.getTimes().intValue();
        for (int i = 0; i < times; i++) {
            repeatCommand.getCommands().forEach(command -> addAbstractCommand(updatedCommand, command));
        }
    }

    public static class Result {

        private static final TreeSet<MappingResult> SET_MAP = new TreeSet<>(new Comparator<MappingResult>() {
            @Override
            public int compare(final MappingResult o1, final MappingResult o2) {
                return getReadonlyValue(o1) ? -1 : 1;
            }

            private boolean getReadonlyValue(final MappingResult result) {
                return nonNull(result.scenario) && (result.scenario.getTags().isReadonly());
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
