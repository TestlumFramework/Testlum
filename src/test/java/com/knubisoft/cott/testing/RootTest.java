package com.knubisoft.cott.testing;

import com.knubisoft.cott.testing.framework.SystemDataStoreCleaner;
import com.knubisoft.cott.testing.framework.TestSetCollector;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.context.SpringTestContext;
import com.knubisoft.cott.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.cott.testing.framework.report.ReportGenerator;
import com.knubisoft.cott.testing.framework.report.ScenarioResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioRunner;
import com.knubisoft.cott.testing.framework.util.FileRemover;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.global_config.DelayBetweenScenariosRuns;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContextManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@SpringBootTest(classes = SpringTestContext.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RootTest {
    @Autowired
    private NameToAdapterAlias nameToAdapterAlias;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private SystemDataStoreCleaner systemDataStoreCleaner;

    @Autowired
    private GlobalScenarioStatCollector globalScenarioStatCollector;

    @Autowired
    private ReportGenerator reportGenerator;

    public static Stream<Arguments> prepareTestData() {
        return new TestSetCollector().collect();
    }

    @BeforeAll
    @SneakyThrows
    public void beforeAll() {
        new TestContextManager(getClass()).prepareTestInstance(this);
        FileRemover.clearActualFiles(TestResourceSettings.getInstance().getScenariosFolder());
        cleanDatabases();
    }

    @DisplayName("Execution of test scenarios:")
    @ParameterizedTest(name = "[{index}] path -- {0}")
    @MethodSource("prepareTestData")
    @SneakyThrows
    void execution(final Named<ScenarioArguments> arguments) {
        ScenarioArguments scenarioArguments = arguments.getPayload();
        cleanDbAndMigrateIfRequired(scenarioArguments.getScenario());
        StopWatch stopWatch = StopWatch.createStarted();
        ScenarioRunner scenarioRunner = new ScenarioRunner(scenarioArguments, ctx);
        ctx.getAutowireCapableBeanFactory().autowireBean(scenarioRunner);
        setTestScenarioResult(stopWatch, scenarioRunner);
    }

    private void setTestScenarioResult(final StopWatch stopWatch, final ScenarioRunner scenarioRunner) {
        ScenarioResult result = globalScenarioStatCollector.addAndReturn(scenarioRunner.run());
        result.setExecutionTime(stopWatch.getTime());
        if (result.getCause() != null) {
            String[] lines = result.getCause().split(System.lineSeparator());
            String message = result.getPath() + " - " + lines[0];
            throw new AssertionError(message, new RuntimeException(result.getCause()));
        }
    }

    private void cleanDbAndMigrateIfRequired(final Scenario scenario) {
        if (!scenario.getTags().isReadonly()) {
            cleanDatabases();
        }
    }

    private void cleanDatabases() {
        systemDataStoreCleaner.cleanAll(this.nameToAdapterAlias);
    }

    @AfterEach
    @SneakyThrows
    public void afterEach() {
        DelayBetweenScenariosRuns timeOut = GlobalTestConfigurationProvider.provide().getDelayBetweenScenariosRuns();
        if (Objects.nonNull(timeOut) && timeOut.isEnabled()) {
            TimeUnit.SECONDS.sleep(timeOut.getSeconds().longValue());
        }
    }

    @AfterAll
    public void afterAll() {
        reportGenerator.generateReport(globalScenarioStatCollector);
    }
}
