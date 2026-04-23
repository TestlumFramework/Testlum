package com.knubisoft.testlum.testing;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.testing.framework.*;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.env.service.EnvironmentExecutionService;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import com.knubisoft.testlum.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.testlum.testing.framework.report.ReportGenerator;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.scenario.InvalidScenarioCondition;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioRunner;
import com.knubisoft.testlum.testing.framework.util.FileRemover;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.global_config.DelayBetweenScenarioRuns;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest
@ComponentScan(basePackageClasses = RootTest.class, basePackages = {"com.knubisoft.testlum.testing.*"})
@Execution(ExecutionMode.CONCURRENT)
@ContextConfiguration(classes = {RootTest.class})
@TestPropertySource(properties = {"spring.main.banner-mode=off"})
@ExtendWith({SpringExtension.class, InvalidScenarioCondition.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RootTest {

    private final ApplicationContext ctx;

    public Stream<Arguments> prepareTestData() {
        return ctx.getBean(TestSetCollector.class).collect();
    }

    @BeforeAll
    public void beforeAll() throws Exception {
        TestResourceSettings testResourceSettings = ctx.getBean(TestResourceSettings.class);
        ctx.getBean(FileRemover.class).clearActualFiles(testResourceSettings.getScenariosFolder());
    }

    @DisplayName("Execution of test scenarios:")
    @ParameterizedTest(name = "[{index}] path -- {0}")
    @MethodSource("prepareTestData")
    void execution(@ConvertWith(ScenarioArgumentsToNamedConverter.class) final Named<ScenarioArguments> arguments) {
        ScenarioArguments data = arguments.getPayload();
        EnvironmentExecutionService executionService = ctx.getBean(EnvironmentExecutionService.class);
        executionService.runInEnvironment(data.getEnvironment(), () -> prepareAndRun(data));
    }

    private void prepareAndRun(final ScenarioArguments args) {
        boolean ignore = args.getException() instanceof IntegrationDisabledException;
        LogUtil logUtil = ctx.getBean(LogUtil.class);
        logUtil.logScenarioDetails(args, args.getException(), ignore ? Color.YELLOW : Color.GREEN);
        if (!ignore) {
            if (args.getScenario().getSettings().isTruncateStorages()) {
                SystemDataStoreCleaner systemDataStoreCleaner = ctx.getBean(SystemDataStoreCleaner.class);

                AliasToStorageOperation aliasToStorageOperation = ctx.getBean(AliasToStorageOperation.class);
                systemDataStoreCleaner.clearAll(aliasToStorageOperation);
            }
            runInstructions(args);
        }
    }

    private void runInstructions(final ScenarioArguments args) {
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
        GlobalScenarioStatCollector globalScenarioStatCollector = ctx.getBean(GlobalScenarioStatCollector.class);
        globalScenarioStatCollector.addResult(result);
        if (StringUtils.isNotBlank(result.getCause())) {
            String[] lines = result.getCause().split(System.lineSeparator());
            String message = result.getPath() + " - " + lines[0];
            throw new AssertionError(result.getCause());
        }
    }

    @AfterEach
    public void afterEach() throws Exception {
        GlobalTestConfiguration globalTestConfiguration = ctx.getBean(GlobalTestConfiguration.class);
        DelayBetweenScenarioRuns delay = globalTestConfiguration.getDelayBetweenScenarioRuns();
        if (delay != null && delay.isEnabled()) {
            TimeUnit.SECONDS.sleep(delay.getSeconds());
        }
    }

    @AfterAll
    public void afterAll() {
        ConnectionManager connectionManager = ctx.getBean(ConnectionManager.class);
        connectionManager.closeConnections();

        Map<String, String> warnings = InvalidScenarioCondition.getRegisteredWarning();
        Map<String, String> errors = InvalidScenarioCondition.getRegisteredError();

        LogUtil logUtil = ctx.getBean(LogUtil.class);
        logUtil.logInvalidScenariosSummary(warnings, errors);

        GlobalScenarioStatCollector globalScenarioStatCollector = ctx.getBean(GlobalScenarioStatCollector.class);
        ReportGenerator reportGenerator = ctx.getBean(ReportGenerator.class);
        reportGenerator.generateReport(globalScenarioStatCollector);

        logInvalidScenariosSummary(errors);
    }

    private void logInvalidScenariosSummary(final Map<String, String> errors) {
        if (!errors.isEmpty()) {
            log.error(LogMessage.INVALID_SCENARIOS_SUMMARY, errors.size());
        }
    }
}