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
import com.knubisoft.testlum.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.testlum.testing.framework.report.ReportGenerator;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioRunner;
import com.knubisoft.testlum.testing.framework.util.FileRemover;
import com.knubisoft.testlum.testing.model.global_config.DelayBetweenScenarioRuns;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Configuration
@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan(basePackageClasses = RootTest.class)
@Execution(ExecutionMode.CONCURRENT)
@ContextConfiguration(classes = RootTest.class)
@TestPropertySource(properties = {"spring.main.banner-mode=off"})
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

    public Stream<Arguments> prepareTestData() {
        return testSetCollector.collect();
    }

    @BeforeAll
    public void beforeAll() throws Exception {
        FileRemover.clearActualFiles(TestResourceSettings.getInstance().getScenariosFolder());
    }

    @DisplayName("Execution of test scenarios:")
    @ParameterizedTest(name = "[{index}] path -- {0}")
    @MethodSource("prepareTestData")
    void execution(@ConvertWith(ScenarioArgumentsToNamedConverter.class) final Named<ScenarioArguments> arguments) {
        envLockService.runLocked(() -> prepareAndRun(arguments.getPayload()));
    }

    private void prepareAndRun(final ScenarioArguments args) {
        args.setEnvironment(EnvManager.currentEnv());
        if (args.getScenario().getSettings().isTruncateStorages()) {
            systemDataStoreCleaner.clearAll(this.aliasToStorageOperation);
        }
        extracted(args);
    }

    private void extracted(final ScenarioArguments args) {
        StopWatch stopWatch = StopWatch.createStarted();
        ScenarioResult result = null;
        try {
            ScenarioRunner scenarioRunner = new ScenarioRunner(args, ctx);
            result = scenarioRunner.run();
            setTestScenarioResult(result);
        } finally {
            stopWatch.stop();
            if (result != null) {
                result.setExecutionTime(stopWatch.getDuration().toMillis());
            }
        }
    }

    private void setTestScenarioResult(final ScenarioResult result) {
        globalScenarioStatCollector.addResult(result);
        if (StringUtils.isNotBlank(result.getCause())) {
            String[] lines = result.getCause().split(System.lineSeparator());
            String message = result.getPath() + " - " + lines[0];
            throw new AssertionError(message, new RuntimeException(result.getCause()));
        }
    }

    @AfterEach
    public void afterEach() throws Exception {
        GlobalTestConfiguration cfg = GlobalTestConfigurationProvider.get().provide();
        DelayBetweenScenarioRuns delay = cfg.getDelayBetweenScenarioRuns();
        if (delay != null && delay.isEnabled()) {
            TimeUnit.SECONDS.sleep(delay.getSeconds());
        }
    }

    @AfterAll
    public void afterAll() {
        connectionManager.closeConnections();
        reportGenerator.generateReport(globalScenarioStatCollector);
    }
}