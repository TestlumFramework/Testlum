package com.knubisoft.testlum.testing;

import com.knubisoft.testlum.testing.framework.ConnectionManager;
import com.knubisoft.testlum.testing.framework.ScenarioArgumentsToNamedConverter;
import com.knubisoft.testlum.testing.framework.SystemDataStoreCleaner;
import com.knubisoft.testlum.testing.framework.TestSetCollector;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.env.service.LockService;
import com.knubisoft.testlum.testing.framework.env.service.impl.EnvLockService;
import com.knubisoft.testlum.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.testlum.testing.framework.report.ReportGenerator;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioRunner;
import com.knubisoft.testlum.testing.framework.util.FileRemover;
import com.knubisoft.testlum.testing.model.global_config.DelayBetweenScenarioRuns;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ExtendWith(SpringExtension.class)
@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RootTest {

    private final ApplicationContext ctx;
    private final TestSetCollector testSetCollector;
    private final LockService envLockService;
    private final SystemDataStoreCleaner systemDataStoreCleaner;
    private final AliasToStorageOperation aliasToStorageOperation;
    private final GlobalScenarioStatCollector globalScenarioStatCollector;
    private final ReportGenerator reportGenerator;
    private final ConnectionManager connectionManager;

    public RootTest() {
        this.ctx = TestResourceSettings.getInstance().getCtx();
        this.testSetCollector = ctx.getBean(TestSetCollector.class);
        this.envLockService = ctx.getBean(EnvLockService.class);
        this.systemDataStoreCleaner = ctx.getBean(SystemDataStoreCleaner.class);
        this.aliasToStorageOperation = ctx.getBean(AliasToStorageOperation.class);
        this.globalScenarioStatCollector = ctx.getBean(GlobalScenarioStatCollector.class);
        this.reportGenerator = ctx.getBean(ReportGenerator.class);
        this.connectionManager = ctx.getBean(ConnectionManager.class);
    }

    @BeforeAll
    public void beforeAll() throws Exception {
        prepareTestInstance();
        removeActualFiles();
    }

    public Stream<Arguments> prepareTestData() {
        return testSetCollector.collect();
    }

    @DisplayName("Execution of test scenarios:")
    @ParameterizedTest(name = "[{index}] path -- {0}")
    @MethodSource("prepareTestData")
    void execution(@ConvertWith(ScenarioArgumentsToNamedConverter.class) final Named<ScenarioArguments> arguments) {
        envLockService.runLocked(() -> execute(arguments.getPayload()));
    }

    private void execute(final ScenarioArguments scenarioArguments) {
        scenarioArguments.setEnvironment(EnvManager.currentEnv());
        clearDataStorages(scenarioArguments.getScenario());
        StopWatch stopWatch = StopWatch.createStarted();
        ScenarioRunner scenarioRunner = new ScenarioRunner(scenarioArguments, ctx);
        ScenarioResult scenarioResult = scenarioRunner.run();
        setTestScenarioResult(stopWatch, scenarioResult);
    }

    private void setTestScenarioResult(final StopWatch stopWatch, final ScenarioResult result) {
        stopWatch.stop();
        result.setExecutionTime(stopWatch.getDuration().toMillis());
        globalScenarioStatCollector.addResult(result);
        if (isNotBlank(result.getCause())) {
            String[] lines = result.getCause().split(System.lineSeparator());
            String message = result.getPath() + " - " + lines[0];
            throw new AssertionError(message, new RuntimeException(result.getCause()));
        }
    }

    private void clearDataStorages(final Scenario scenario) {
        if (scenario.getSettings().isTruncateStorages()) {
            systemDataStoreCleaner.clearAll(this.aliasToStorageOperation);
        }
    }

    private void prepareTestInstance() throws Exception {
        new TestContextManager(getClass()).prepareTestInstance(this);
    }

    private void removeActualFiles() throws IOException {
        File scenarioFolder = TestResourceSettings.getInstance().getScenariosFolder();
        FileRemover.clearActualFiles(scenarioFolder);
    }

    @AfterEach
    public void afterEach() throws Exception {
        DelayBetweenScenarioRuns delay = GlobalTestConfigurationProvider.get().
                provide().getDelayBetweenScenarioRuns();
        if (nonNull(delay) && delay.isEnabled()) {
            TimeUnit.SECONDS.sleep(delay.getSeconds());
        }
    }

    @AfterAll
    public void afterAll() {
        connectionManager.closeConnections();
        reportGenerator.generateReport(globalScenarioStatCollector);
    }
}