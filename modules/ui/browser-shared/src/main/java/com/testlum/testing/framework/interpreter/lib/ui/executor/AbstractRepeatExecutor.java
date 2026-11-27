package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.SubCommandRunner;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.variations.GlobalVariations;
import com.testlum.testing.model.scenario.AbstractUiCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
public abstract class AbstractRepeatExecutor<T extends AbstractUiCommand>
        extends AbstractUiExecutor<T> {

    private static final String COMMAND_REPEAT_FINISHED_LOG =
            LogFormat.withYellow("------- Repeat is finished -------");
    private static final String COMMAND_REPEAT_WITH_INDEX_RUN_LOG =
            LogFormat.withCyan("------- Repeat Run %d/%d -------");
    private static final String COMMAND_REPEAT_WITH_VARIATION_RUN_LOG =
            LogFormat.withCyan("------- Repeat Variation %d/%d: %s -------");

    private final SubCommandRunner repeatCommandsRunner;
    private final GlobalVariations globalVariations;

    protected AbstractRepeatExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.repeatCommandsRunner = dependencies.getContext().getBean(SubCommandRunner.class);
        this.globalVariations = dependencies.getContext().getBean(GlobalVariations.class);
    }

    protected abstract List<AbstractUiCommand> getCommands(T repeat);

    protected abstract Integer getTimes(T repeat);

    protected abstract String getVariations(T repeat);

    @Override
    public void execute(final T repeat, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        validateRepeatType(repeat);
        if (StringUtils.isNotBlank(getVariations(repeat))) {
            runRepeatWithVariations(repeat, result, subCommandsResult);
        } else {
            runSimpleRepeat(repeat, result, subCommandsResult);
        }
        log.info(LogMessage.REPEAT_FINISHED_LOG);
    }

    private void runRepeatWithVariations(final T repeat,
                                         final CommandResult result,
                                         final List<CommandResult> subCommandsResult) {
        log.info(LogFormat.table("Variations", getVariations(repeat)));
        result.put("Variations", getVariations(repeat));
        List<AbstractUiCommand> commands = getCommands(repeat);
        List<Map<String, String>> variations = globalVariations.getVariations(getVariations(repeat));
        for (int i = 0; i < variations.size(); i++) {
            Map<String, String> variationMap = variations.get(i);
            log.info(format(COMMAND_REPEAT_WITH_VARIATION_RUN_LOG, i + 1, variations.size(), variationMap.toString()));
            List<AbstractUiCommand> injectedCommand = commands.stream()
                    .map(command -> scenarioInjectionUtil.injectObjectVariation(
                            command, variationMap, dependencies.getScenarioContext()))
                    .toList();
            this.repeatCommandsRunner.runCommands(injectedCommand, dependencies, result, subCommandsResult);
        }

    }

    private void runSimpleRepeat(final T repeat,
                                 final CommandResult result,
                                 final List<CommandResult> subCommandsResult) {
        log.info(LogFormat.table("Times", String.valueOf(getTimes(repeat))));
        result.put("Times", getTimes(repeat));
        for (int i = 0; i < getTimes(repeat); i++) {
            log.info(format(COMMAND_REPEAT_WITH_INDEX_RUN_LOG, i + 1, getTimes(repeat)));
            this.repeatCommandsRunner.runCommands(
                    getCommands(repeat), dependencies, result, subCommandsResult);
        }
    }

    private void validateRepeatType(final T repeat) {
        Integer times = getTimes(repeat);
        String variations = getVariations(repeat);
        if (times == null && StringUtils.isBlank(variations)) {
            throw new DefaultFrameworkException(ExceptionMessage.REPEAT_TYPE_IS_NOT_PROVIDED);
        }
    }
}
