package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsImpl.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import com.knubisoft.testlum.testing.model.scenario.Include;
import com.knubisoft.testlum.testing.model.scenario.Logout;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_NOT_FOUND;
import static java.util.Objects.nonNull;

@Slf4j
public class ScenarioCollector {

    private final File rootTestResources;
    private final Optional<File> scenarioScopeFolder;
    private final ScenarioValidator scenarioValidator;

    public ScenarioCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.rootTestResources = resourceSettings.getTestResourcesFolder();
        this.scenarioScopeFolder = resourceSettings.getScenarioScopeFolder();
        this.scenarioValidator = new ScenarioValidator();
    }

    public Result collect() {
        List<File> scenarios = new ArrayList<>();
        if (scenarioScopeFolder.isPresent()) {
            walk(scenarioScopeFolder.get(), scenarios);
        } else {
            walk(rootTestResources, scenarios);
        }
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
        Scenario scenario = null;
        try {
            scenario = convertXmlToScenario(xmlFile);
            scenarioValidator.validate(scenario, xmlFile);
            result.add(new MappingResult(xmlFile, scenario, null));
        } catch (IntegrationDisabledException e) {
            result.add(new MappingResult(xmlFile, scenario, e));
        } catch (Exception e) {
            result.add(new MappingResult(xmlFile, null, e));
        }
    }

    private Scenario convertXmlToScenario(final File xmlFile) {
        Scenario scenario = XMLParsers.getInstance().forScenario().process(xmlFile);
        Optional<String> variations = getScenarioVariations(xmlFile, scenario);
        updateScenario(scenario, variations);
        return scenario;
    }

    private Optional<String> getScenarioVariations(final File xmlFile, final Scenario scenario) {
        if (nonNull(scenario.getSettings().getVariations())) {
            GlobalVariationsProvider.process(scenario, xmlFile);
            return Optional.of(scenario.getSettings().getVariations());
        } else {
            return Optional.empty();
        }
    }

    private void updateScenario(final Scenario scenario, final Optional<String> variationFileName) {
        List<AbstractCommand> updatedCommands = updateCommands(scenario.getCommands(), variationFileName);
        scenario.getCommands().clear();
        scenario.getCommands().addAll(updatedCommands);
    }

    private List<AbstractCommand> updateCommands(final List<AbstractCommand> commands,
                                                 final Optional<String> variationFileName) {
        List<AbstractCommand> updatedCommands = new ArrayList<>();
        for (AbstractCommand command : commands) {
            addAbstractCommand(updatedCommands, command, variationFileName);
        }
        return updatedCommands;
    }

    private void addAbstractCommand(final List<AbstractCommand> updatedCommand,
                                    final AbstractCommand command,
                                    final Optional<String> variationFileName) {
        if (command instanceof Auth) {
            addAuthCommands(updatedCommand, (Auth) command);
        } else if (command instanceof Include) {
            addIncludeCommands(updatedCommand, command, variationFileName);
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
        List<Api> apiList = GlobalTestConfigurationProvider.get().getDefaultIntegrations().getApis().getApi();
        Api apiIntegration = IntegrationsUtil.findApiForAlias(apiList, alias);
        if (nonNull(apiIntegration.getAuth())) {
            return apiIntegration.getAuth().isAutoLogout();
        }
        throw new DefaultFrameworkException(AUTH_NOT_FOUND, apiIntegration.getAlias());
    }

    private void addIncludeCommands(final List<AbstractCommand> updatedCommands,
                                    final AbstractCommand command,
                                    final Optional<String> variationFileName) {
        Include include = getIncludeCommand(command, variationFileName);
        Scenario includedScenario = findIncludedScenarioAndParse(include);
        updateScenario(includedScenario, variationFileName);
        updatedCommands.addAll(includedScenario.getCommands());
    }

    private Include getIncludeCommand(final AbstractCommand command, final Optional<String> variationFileName) {
        Include include = (Include) command;
        if (variationFileName != null && variationFileName.isPresent()) {
            List<Map<String, String>> variationList = GlobalVariationsProvider.getVariations(variationFileName.get());
            include = variationList.stream()
                    .map(variationMap -> (Include) InjectionUtil.injectObjectVariation(command, variationMap))
                    .findFirst().get();
        }
        return include;
    }

    private Scenario findIncludedScenarioAndParse(final Include include) {
        File scenariosFolder = TestResourceSettings.getInstance().getScenariosFolder();
        File includedScenarioFolder = new File(scenariosFolder,
                include.getScenario());
        File file = FileSearcher.searchFileFromDir(includedScenarioFolder, TestResourceSettings.SCENARIO_FILENAME);
        return XMLParsers.getInstance().forScenario().process(file, scenarioValidator);
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
