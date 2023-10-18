package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public abstract class AbstractInterpreter<T extends AbstractCommand> {

    private static final String JSON_EXTENSION = ".json";
    private static final String ACTUAL_FILENAME = "actual.json";
    private static final String FILENAME_TO_SAVE = "action_%s_" + ACTUAL_FILENAME;
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String COMMENT_LOG = format(TABLE_FORMAT, "Comment", "{}");
    private static final String SLOW_COMMAND_PROCESSING = "Slow command processing detected. "
            + "Took %d ms, threshold %d ms";
    private static final String POSITION_COMMAND_LOG = ANSI_YELLOW
            + "--------- Scenario step #{} - {} ---------" + ANSI_RESET;
    private static final String COMMAND_LOG_WITHOUT_POSITION = ANSI_YELLOW
            + "--------- Scenario step - {} ---------" + ANSI_RESET;
    protected final InterpreterDependencies dependencies;
    protected final ConfigProvider configurationProvider;
    protected final ConditionProvider conditionProvider;

    protected AbstractInterpreter(final InterpreterDependencies dependencies) {
        this.dependencies = dependencies;
        this.configurationProvider = dependencies.getContext().getBean(ConfigProvider.class);
        this.conditionProvider = dependencies.getContext().getBean(ConditionProvider.class);
    }

    public final void apply(final T o, final CommandResult result) {
        logCommand(result.getId(), o);
        if (isNotBlank(o.getComment())) {
            String comment = inject(o.getComment());
            log.info(COMMENT_LOG, comment);
            result.setComment(comment);
        }
        if (conditionProvider.isTrue(inject(o.getCondition()), dependencies.getScenarioContext(), result)) {
            checkExecutionTime(o, () -> acceptImpl(o, result));
        }
    }

    private void checkExecutionTime(final T o, final Runnable r) {
        StopWatch sw = StopWatch.createStarted();
        r.run();
        long ms = sw.getTime(TimeUnit.MILLISECONDS);
        Integer threshold = o.getThreshold();
        if (nonNull(threshold) && ms > threshold) {
            throw new DefaultFrameworkException(SLOW_COMMAND_PROCESSING, ms, threshold);
        }
    }

    protected abstract void acceptImpl(T o, CommandResult result);

    public CompareBuilder newCompare() {
        return new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get());
    }

    public void save(final String actual) {
        try {
            File target = new File(dependencies.getFile().getParent(),
                    format(FILENAME_TO_SAVE, dependencies.getPosition().get()));
            FileUtils.writeStringToFile(target, StringPrettifier.prettifyToSave(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }

    public String toString(final Object o) {
        return JacksonMapperUtil.writeValueAsString(o);
    }

    public String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    public String getContentIfFile(final String fileOrContent) {
        if (isNotBlank(fileOrContent) && fileOrContent.endsWith(JSON_EXTENSION)) {
            String content = FileSearcher.searchFileToString(fileOrContent, dependencies.getFile());
            return inject(content);
        }
        return fileOrContent;
    }

    protected void setContextBody(final String o) {
        dependencies.getScenarioContext().setBody(o);
    }

    @SuppressWarnings("unchecked")
    protected <Y> Y injectCommand(final Y o) {
        if (nonNull(o)) {
            String asJson = JacksonMapperUtil.writeValueToCopiedString(o);
            String injected = dependencies.getScenarioContext().inject(asJson);
            return JacksonMapperUtil.readCopiedValue(injected, (Class<Y>) o.getClass());
        }
        return null;
    }

    protected void checkIfStopScenarioOnFailure(final Exception e) {
        if (configurationProvider.provide().isStopScenarioOnFailure()) {
            throw new DefaultFrameworkException(e);
        }
    }

    private void logCommand(final long position, final AbstractCommand command) {
        if (position != 0) {
            log.info(POSITION_COMMAND_LOG, position, command.getClass().getSimpleName());
        } else {
            log.info(COMMAND_LOG_WITHOUT_POSITION, command.getClass().getSimpleName());
        }
    }
}
