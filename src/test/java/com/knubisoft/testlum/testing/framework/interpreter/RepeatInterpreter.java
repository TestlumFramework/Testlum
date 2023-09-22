package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.RepeatCommandsRunner;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Repeat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_REPEAT_FINISHED_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TABLE_FORMAT;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Repeat.class)
public class RepeatInterpreter extends AbstractInterpreter<Repeat> {

    @Autowired
    private RepeatCommandsRunner repeatCommandsRunner;

    public RepeatInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Repeat repeat, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        if (StringUtils.isNotBlank(repeat.getVariations())) {
            runRepeatWithVariations(repeat, result, subCommandsResult);
        } else {
           runSimpleRepeat(repeat, result, subCommandsResult);
        }
        log.info(COMMAND_REPEAT_FINISHED_LOG);
    }

    private void runRepeatWithVariations(final Repeat repeat,
                                         final CommandResult result,
                                         final List<CommandResult> subCommandsResult) {
        log.info(format(TABLE_FORMAT, "Variations", repeat.getVariations()));
        result.put("Variations", repeat.getVariations());
        List<AbstractCommand> commands = repeat.getCommands();
        List<AbstractCommand> injectedCommand = GlobalVariations.getVariations(repeat.getVariations()).stream()
                .flatMap(variation -> commands.stream().map(command ->
                        InjectionUtil.injectObjectVariation(command, variation, dependencies.getScenarioContext())))
                .collect(Collectors.toList());
        this.repeatCommandsRunner.runCommands(injectedCommand, dependencies, result, subCommandsResult);
    }

    private void runSimpleRepeat(final Repeat repeat,
                                 final CommandResult result,
                                 final List<CommandResult> subCommandsResult) {
        Repeat repeat1 = injectCommand(repeat);
        log.info(format(TABLE_FORMAT, "Times", repeat.getTimes()));
        result.put("Times", repeat.getTimes());
        for (int i = 0; i < repeat1.getTimes(); i++) {
            this.repeatCommandsRunner.runCommands(repeat1.getCommands(), dependencies, result, subCommandsResult);
        }
    }
}
