package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import com.knubisoft.testlum.testing.framework.util.ScenarioInjectionUtil;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioCollector {

    private final ScenarioValidator scenarioValidator;
    private final XMLParsers xmlParsers;
    private final TestResourceSettings testResourceSettings;
    private final IntegrationsUtil integrationUtil;
    private final FileSearcher fileSearcher;
    private final GlobalVariationsProvider globalVariationsProvider;
    private final ScenarioInjectionUtil scenarioInjectionUtil;
    private final Integrations integrations;

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
        Scenario scenario = xmlParsers.forScenario().process(xmlFile);
        Optional<String> variations = getScenarioVariations(xmlFile, scenario);
        updateScenario(scenario, variations);
        return scenario;
    }

    private Optional<String> getScenarioVariations(final File xmlFile, final Scenario scenario) {
        if (Objects.nonNull(scenario.getSettings())
                && Objects.nonNull(scenario.getSettings().getVariations())) {
            globalVariationsProvider.process(scenario, xmlFile);
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
        if (command instanceof Auth auth) {
            addAuthCommands(updatedCommand, auth);
        } else if (command instanceof Include include) {
            addIncludeCommands(updatedCommand, include, variationFileName);
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
        List<Api> apiList = integrations.getApis().getApi();
        Api apiIntegration = integrationUtil.findApiForAlias(apiList, alias);
        if (Objects.nonNull(apiIntegration.getAuth())) {
            return apiIntegration.getAuth().isAutoLogout();
        }
        throw new DefaultFrameworkException(ExceptionMessage.AUTH_NOT_FOUND, apiIntegration.getAlias());
    }

    private void addIncludeCommands(final List<AbstractCommand> updatedCommands,
                                    final Include command,
                                    final Optional<String> variationFileName) {
        Include include = getIncludeCommand(command, variationFileName);
        Scenario includedScenario = findIncludedScenarioAndParse(include);
        updateScenario(includedScenario, variationFileName);
        updatedCommands.addAll(includedScenario.getCommands());
    }

    private Include getIncludeCommand(final Include include, final Optional<String> variationFileName) {
        if (variationFileName != null && variationFileName.isPresent()) {
            List<Map<String, String>> variationList = globalVariationsProvider.getVariations(variationFileName.get());
            return variationList.stream()
                    .map(variationMap -> (Include) scenarioInjectionUtil.injectObjectVariation(include, variationMap))
                    .findFirst()
                    .orElseThrow(() -> new DefaultFrameworkException("No variations found for include command"));
        }
        return include;
    }

    private Scenario findIncludedScenarioAndParse(final Include include) {
        File scenariosFolder = testResourceSettings.getScenariosFolder();
        File includedScenarioFolder = new File(scenariosFolder,
                include.getScenario());
        File file = fileSearcher.searchFileFromDir(includedScenarioFolder, TestResourceSettings.SCENARIO_FILENAME);
        return xmlParsers.forScenario().process(file, scenarioValidator);
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
