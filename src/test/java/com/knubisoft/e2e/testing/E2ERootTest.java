package com.knubisoft.e2e.testing;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.context.SpringTestContext;
import com.knubisoft.e2e.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.e2e.testing.framework.report.ReportGenerator;
import com.knubisoft.e2e.testing.framework.report.ScenarioResult;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioRunner;
import com.knubisoft.e2e.testing.framework.util.FileRemover;
import com.knubisoft.e2e.testing.framework.util.LogMessage;
import com.knubisoft.e2e.testing.model.ScenarioArguments;
import com.knubisoft.e2e.testing.model.scenario.Overview;
import com.knubisoft.e2e.testing.framework.SystemDataStoreCleaner;
import com.knubisoft.e2e.testing.framework.TestSetCollector;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.internal.AssumptionViolatedException;
import org.junit.jupiter.api.AfterAll;
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

import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest(classes = SpringTestContext.class)
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class E2ERootTest {
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
        executeScenario(scenarioArguments);
    }

    private void executeScenario(final ScenarioArguments scenarioArguments) {
        cleanDbAndMigrateIfRequired(scenarioArguments.getScenario());
        StopWatch stopWatch = StopWatch.createStarted();
        ScenarioRunner scenarioRunner =
                new ScenarioRunner(scenarioArguments, ctx);
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


    private AssumptionViolatedException assumptionViolatedException(final String path, final Scenario scenario) {
        String message = Optional.ofNullable(scenario.getOverview())
                .map(Overview::getDescription)
                .orElse(String.format(LogMessage.TEST_BY_PATH_DISABLED, path));
        return new AssumptionViolatedException(message);
    }

    @AfterAll
    @SneakyThrows
    public void afterAll() {
        reportGenerator.generateReport();
    }
}
