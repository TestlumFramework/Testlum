package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Condition;
import com.knubisoft.testlum.testing.model.scenario.FromExpression;
import com.knubisoft.testlum.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractInterpreter<T extends AbstractCommand> {

    protected static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    private static final String JSON_EXTENSION = ".json";
    private static final String ACTUAL_FILENAME = "actual.json";
    private static final String FILENAME_TO_SAVE = "action_%s_" + ACTUAL_FILENAME;

    private static final String COMMENT_LOG = LogFormat.table("Comment");
    private static final String SLOW_COMMAND_PROCESSING =
            "Slow command processing detected. Took %d ms, threshold %d ms";
    private static final String POSITION_COMMAND_LOG =
            LogFormat.withYellow("--------- Scenario step #{} - {} ---------");
    private static final String COMMAND_LOG_WITHOUT_POSITION =
            LogFormat.withYellow("--------- Scenario step - {} ---------");

    protected final InterpreterDependencies dependencies;
    protected final ConfigProvider configurationProvider;
    protected final ConditionProvider conditionProvider;
    protected final FileSearcher fileSearcher;
    protected final JacksonService jacksonService;
    protected final StringPrettifier stringPrettifier;

    private final boolean stopScenarioOnFailure;

    protected AbstractInterpreter(final InterpreterDependencies dependencies) {
        this.dependencies = dependencies;
        this.configurationProvider = dependencies.getContext().getBean(ConfigProvider.class);
        this.conditionProvider = dependencies.getContext().getBean(ConditionProvider.class);
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
        this.jacksonService = dependencies.getContext().getBean(JacksonService.class);
        this.stringPrettifier = dependencies.getContext().getBean(StringPrettifier.class);
        this.stopScenarioOnFailure = dependencies.getContext().
                getBean(GlobalTestConfiguration.class).isStopScenarioOnFailure();
    }

    public final void apply(final T o, final CommandResult result) {
        logCommand(result.getId(), o);
        if (StringUtils.isNotBlank(o.getComment())) {
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
        if (Objects.nonNull(threshold) && ms > threshold) {
            throw new DefaultFrameworkException(SLOW_COMMAND_PROCESSING, ms, threshold);
        }
    }

    protected abstract void acceptImpl(T o, CommandResult result);

    public CompareBuilder newCompare() {
        return new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get(),
                jacksonService, stringPrettifier);
    }

    public void save(final String actual) {
        try {
            File target = new File(dependencies.getFile().getParent(),
                    String.format(FILENAME_TO_SAVE, dependencies.getPosition().get()));
            FileUtils.writeStringToFile(target, stringPrettifier.prettifyToSave(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }

    public String toString(final Object o) {
        return jacksonService.writeValueAsString(o);
    }

    public String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    public String getContentIfFile(final String fileOrContent) {
        if (StringUtils.isNotBlank(fileOrContent) && fileOrContent.endsWith(JSON_EXTENSION)) {
            String content = fileSearcher.searchFileToString(fileOrContent, dependencies.getFile());
            return inject(content);
        }
        return fileOrContent;
    }

    public String getContextBodyKey(final String fileOrContent) {
        if (StringUtils.isNotBlank(fileOrContent)) {
            return fileOrContent;
        }
        return UUID.randomUUID().toString();
    }

    protected void setContextBody(final String key, final String body) {
        dependencies.getScenarioContext().set(key, body);
    }

    @SuppressWarnings("unchecked")
    protected <Y> Y injectCommand(final Y o) {
        if (Objects.isNull(o)) {
            return null;
        }
        String asJson = jacksonService.writeValueToCopiedString(o);
        String injected = dependencies.getScenarioContext().inject(asJson);
        return jacksonService.readCopiedValue(injected, (Class<Y>) o.getClass());
    }

    protected Var injectVarCommand(final Var var) {
        if (Objects.isNull(var)) {
            return null;
        }
        Var varCopy = jacksonService.deepCopy(var, Var.class);
        FromExpression expression = varCopy.getExpression();
        if (Objects.nonNull(expression)) {
            expression.setValue(dependencies.getScenarioContext().injectSpel(expression.getValue()));
        }
        return injectCommand(varCopy);
    }

    protected Condition injectConditionCommand(final Condition condition) {
        if (Objects.isNull(condition)) {
            return null;
        }
        Condition conditionCopy = jacksonService.deepCopy(condition, Condition.class);
        conditionCopy.setSpel(dependencies.getScenarioContext().injectSpel(conditionCopy.getSpel()));
        return injectCommand(conditionCopy);
    }

    protected void checkIfStopScenarioOnFailure(final Exception e) {
        if (stopScenarioOnFailure) {
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

    protected void logException(final Exception ex) {
        String msg = StringUtils.isNotBlank(ex.getMessage())
                ? ex.getMessage().replaceAll(LogFormat.newLine(), LogFormat.newLogLine()) : ex.toString();
        log.error(LogFormat.exceptionLog(), msg);
    }

    protected void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    protected void ensureAlias(final Supplier<String> getAlias, final Consumer<String> setAlias) {
        if (getAlias.get() == null) {
            setAlias.accept(DEFAULT_ALIAS_VALUE);
        }
    }
}