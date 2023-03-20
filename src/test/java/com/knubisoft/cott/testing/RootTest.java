package com.knubisoft.cott.testing;

import com.knubisoft.cott.testing.framework.SystemDataStoreCleaner;
import com.knubisoft.cott.testing.framework.TestSetCollector;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.context.SpringTestContext;
import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.env.service.LockService;
import com.knubisoft.cott.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.cott.testing.framework.report.ReportGenerator;
import com.knubisoft.cott.testing.framework.report.ScenarioResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioRunner;
import com.knubisoft.cott.testing.framework.util.FileRemover;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.global_config.DelayBetweenScenarioRuns;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContextManager;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@SpringBootTest(classes = SpringTestContext.class)
@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RootTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private TestSetCollector testSetCollector;

    @Autowired
    private LockService envLockService;

    @Autowired
    private SystemDataStoreCleaner systemDataStoreCleaner;

    @Autowired
    private NameToAdapterAlias nameToAdapterAlias;

    @Autowired
    private GlobalScenarioStatCollector globalScenarioStatCollector;

    @Autowired
    private ReportGenerator reportGenerator;

    @BeforeAll
    @SneakyThrows
    public void beforeAll() {
        new TestContextManager(getClass()).prepareTestInstance(this);
        FileRemover.clearActualFiles(TestResourceSettings.getInstance().getScenariosFolder());
    }

    public Stream<Arguments> prepareTestData() {
        return testSetCollector.collect();
    }

    @DisplayName("Execution of test scenarios:")
    @ParameterizedTest(name = "[{index}] path -- {0}")
    @MethodSource("prepareTestData")
    void execution(final Named<ScenarioArguments> arguments) {
        envLockService.runLocked(() -> execute(arguments.getPayload()));
    }

    private void execute(final ScenarioArguments scenarioArguments) {
        scenarioArguments.setEnvironment(EnvManager.currentEnv());
        ScenarioRunner scenarioRunner = new ScenarioRunner(scenarioArguments, ctx);
        StopWatch stopWatch = StopWatch.createStarted();
        clearDataStorages(scenarioArguments.getScenario());
        ScenarioResult scenarioResult = scenarioRunner.run();
        setTestScenarioResult(stopWatch, scenarioResult);
    }

    private void setTestScenarioResult(final StopWatch stopWatch, final ScenarioResult result) {
        result.setExecutionTime(stopWatch.getTime());
        stopWatch.stop();
        globalScenarioStatCollector.addResult(result);
        if (result.getCause() != null) {
            String[] lines = result.getCause().split(System.lineSeparator());
            String message = result.getPath() + " - " + lines[0];
            throw new AssertionError(message, new RuntimeException(result.getCause()));
        }
    }

    private void clearDataStorages(final Scenario scenario) {
        if (!scenario.getTags().isReadonly()) {
            systemDataStoreCleaner.clearAll(this.nameToAdapterAlias);
        }
    }

    @AfterEach
    @SneakyThrows
    public void afterEach() {
        DelayBetweenScenarioRuns delay = GlobalTestConfigurationProvider.provide().getDelayBetweenScenarioRuns();
        if (nonNull(delay) && delay.isEnabled()) {
            TimeUnit.SECONDS.sleep(delay.getSeconds());
        }
    }

    @AfterAll
    public void afterAll() {
        reportGenerator.generateReport(globalScenarioStatCollector);
    }
}
