package com.testlum.testing.framework.interpreter.lib;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.AuthCommandExpander;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.scenario.ScenarioValidator;
import com.testlum.testing.framework.util.ConfigUtil;
import com.testlum.testing.framework.util.LogUtil;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.xml.XMLParser;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Include;
import com.testlum.testing.model.scenario.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncludedScenarioRunnerImplTest {

    @Mock
    private XMLParsers xmlParsers;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private ConfigUtil configUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private InterpreterProvider interpreterProvider;
    @Mock
    private ScenarioValidator scenarioValidator;
    @Mock
    private AuthCommandExpander authCommandExpander;

    private IncludedScenarioRunnerImpl runner;

    @BeforeEach
    void setUp() {
        runner = new IncludedScenarioRunnerImpl(
                xmlParsers,
                fileSearcher,
                testResourceSettings,
                resultUtil,
                configUtil,
                logUtil,
                interpreterProvider,
                scenarioValidator,
                authCommandExpander
        );
    }

    @Nested
    class Run {

        @Test
        void updatesDependenciesWithIncludedFile() {
            final File scenariosFolder = new File("/tests/scenarios");
            when(testResourceSettings.getScenariosFolder()).thenReturn(scenariosFolder);

            final Include include = new Include();
            include.setScenario("included_folder/scenario.xml");

            final File includedFile = new File("/tests/scenarios/included_folder/scenario.xml");
            when(fileSearcher.searchFileFromDir(any(File.class), eq(TestResourceSettings.SCENARIO_FILENAME)))
                    .thenReturn(includedFile);

            @SuppressWarnings("unchecked")
            final XMLParser<Scenario> scenarioParser = mock(XMLParser.class);
            when(xmlParsers.forScenario()).thenReturn(scenarioParser);

            final Scenario scenario = new Scenario();
            final AbstractCommand mockCommand = mock(AbstractCommand.class);
            scenario.getCommands().add(mockCommand);
            when(scenarioParser.process(includedFile, scenarioValidator)).thenReturn(scenario);
            when(authCommandExpander.expand(anyList())).thenReturn(List.of(mockCommand));

            final ApplicationContext ctx = mock(ApplicationContext.class);
            final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
            when(ctx.getAutowireCapableBeanFactory()).thenReturn(beanFactory);

            final File mainFile = new File("/tests/scenarios/main/scenario.xml");
            final InterpreterDependencies mainDeps = InterpreterDependencies.builder()
                    .context(ctx)
                    .file(mainFile)
                    .scenarioContext(new ScenarioContext(new HashMap<>()))
                    .position(new AtomicInteger(0))
                    .environment("local")
                    .build();

            final CommandResult result = new CommandResult();
            result.setSubCommandsResult(new ArrayList<>());
            when(resultUtil.newCommandResultInstance(anyInt(), eq(mockCommand))).thenReturn(new CommandResult());

            @SuppressWarnings("unchecked")
            final AbstractInterpreter<AbstractCommand> mockInterpreter = mock(AbstractInterpreter.class);
            when(interpreterProvider.getAppropriateInterpreter(eq(mockCommand), any(InterpreterDependencies.class)))
                    .thenReturn(mockInterpreter);

            runner.run(include, mainDeps, result);

            final ArgumentCaptor<InterpreterDependencies> captor =
                    ArgumentCaptor.forClass(InterpreterDependencies.class);
            verify(interpreterProvider).getAppropriateInterpreter(eq(mockCommand), captor.capture());

            final InterpreterDependencies capturedDeps = captor.getValue();
            assertEquals(includedFile, capturedDeps.getFile(),
                    "Included scenario commands must resolve files relative to the included scenario file");
            assertEquals(mainDeps.getContext(), capturedDeps.getContext());
            assertEquals(mainDeps.getPosition(), capturedDeps.getPosition());
            assertEquals(mainDeps.getScenarioContext(), capturedDeps.getScenarioContext());
            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
        }

        @Test
        void throwsExceptionWhenCycleDetected() {
            final File scenariosFolder = new File("/tests/scenarios");
            when(testResourceSettings.getScenariosFolder()).thenReturn(scenariosFolder);

            final Include include = new Include();
            include.setScenario("recursive/scenario.xml");

            final File includedFile = new File("/tests/scenarios/recursive/scenario.xml");
            when(fileSearcher.searchFileFromDir(any(File.class), eq(TestResourceSettings.SCENARIO_FILENAME)))
                    .thenReturn(includedFile);

            @SuppressWarnings("unchecked")
            final XMLParser<Scenario> scenarioParser = mock(XMLParser.class);
            when(xmlParsers.forScenario()).thenReturn(scenarioParser);

            final Scenario scenario = new Scenario();
            when(scenarioParser.process(includedFile, scenarioValidator)).thenReturn(scenario);
            when(authCommandExpander.expand(anyList())).thenAnswer(invocation -> {
                runner.run(include, mock(InterpreterDependencies.class), new CommandResult());
                return List.of();
            });

            final InterpreterDependencies mainDeps = InterpreterDependencies.builder()
                    .file(new File("/tests/scenarios/main/scenario.xml"))
                    .build();

            final CommandResult result = new CommandResult();
            result.setSubCommandsResult(new ArrayList<>());

            assertThrows(DefaultFrameworkException.class, () -> runner.run(include, mainDeps, result));
        }
    }
}
