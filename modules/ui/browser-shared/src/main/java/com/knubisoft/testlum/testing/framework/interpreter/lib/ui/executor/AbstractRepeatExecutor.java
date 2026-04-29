package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public abstract class AbstractRepeatExecutor<T extends AbstractUiCommand>
        extends AbstractUiExecutor<T> {

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
        List<AbstractUiCommand> injectedCommand =
                globalVariations.getVariations(getVariations(repeat)).stream()
                .flatMap(variation -> commands.stream()
                        .map(command -> scenarioInjectionUtil
                                .injectObjectVariation(command, variation,
                                        dependencies.getScenarioContext())))
                .toList();
        this.repeatCommandsRunner.runCommands(
                injectedCommand, dependencies, result, subCommandsResult);
    }

    private void runSimpleRepeat(final T repeat,
                                 final CommandResult result,
                                 final List<CommandResult> subCommandsResult) {
        log.info(LogFormat.table("Times", String.valueOf(getTimes(repeat))));
        result.put("Times", getTimes(repeat));
        for (int i = 0; i < getTimes(repeat); i++) {
            this.repeatCommandsRunner.runCommands(
                    getCommands(repeat), dependencies, result, subCommandsResult);
        }
    }
}
