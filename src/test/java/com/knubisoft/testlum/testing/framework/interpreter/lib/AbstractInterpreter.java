package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.MigrationConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SLOW_COMMAND_PROCESSING;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMENT_LOG;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public abstract class AbstractInterpreter<T extends AbstractCommand> {

    protected final InterpreterDependencies dependencies;

    protected AbstractInterpreter(final InterpreterDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        LogUtil.logCommand(result.getId(), o);
        if (isNotBlank(o.getComment())) {
            String comment = inject(o.getComment());
            log.info(COMMENT_LOG, comment);
            result.setComment(comment);
        }
        if (ConditionUtil.isTrue(inject(o.getCondition()), dependencies.getScenarioContext(), result)) {
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
                    format(TestResourceSettings.FILENAME_TO_SAVE, dependencies.getPosition().get()));
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
        if (isNotBlank(fileOrContent) && fileOrContent.endsWith(MigrationConstant.JSON_EXTENSION)) {
            String content = FileSearcher.searchFileToString(fileOrContent, dependencies.getFile());
            return inject(content);
        }
        return fileOrContent;
    }

    protected void setContextBody(final String o) {
        dependencies.getScenarioContext().setBody(o);
    }

    protected <Y> Y injectCommand(final Y o) {
        if (nonNull(o)) {
            return InjectionUtil.injectObject(o, dependencies.getScenarioContext());
        }
        return null;
    }
}
