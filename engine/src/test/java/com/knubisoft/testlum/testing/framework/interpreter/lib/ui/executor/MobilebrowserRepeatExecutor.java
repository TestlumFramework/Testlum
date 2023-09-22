package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.MobilebrowserRepeat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.REPEAT_FINISHED_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TABLE_FORMAT;
import static java.lang.String.format;

@Slf4j
@ExecutorForClass(MobilebrowserRepeat.class)
public class MobilebrowserRepeatExecutor extends AbstractUiExecutor<MobilebrowserRepeat> {

    @Autowired
    private SubCommandRunner repeatCommandsRunner;

    public MobilebrowserRepeatExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final MobilebrowserRepeat repeat, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        if (StringUtils.isNotBlank(repeat.getVariations())) {
            runRepeatWithVariations(repeat, result, subCommandsResult);
        } else {
            runSimpleRepeat(repeat, result, subCommandsResult);
        }
        log.info(REPEAT_FINISHED_LOG);
    }

    private void runRepeatWithVariations(final MobilebrowserRepeat repeat,
                                         final CommandResult result,
                                         final List<CommandResult> subCommandsResult) {
        log.info(format(TABLE_FORMAT, "Variations", repeat.getVariations()));
        result.put("Variations", repeat.getVariations());
        List<AbstractUiCommand> commands = repeat.getClickOrInputOrAssert();
        List<AbstractUiCommand> injectedCommand = GlobalVariations.getVariations(repeat.getVariations()).stream()
                .flatMap(variation -> commands.stream().map(command ->
                        InjectionUtil.injectObjectVariation(command, variation)))
                .collect(Collectors.toList());
        this.repeatCommandsRunner.runCommands(injectedCommand, dependencies, result, subCommandsResult);
    }

    private void runSimpleRepeat(final MobilebrowserRepeat repeat,
                                 final CommandResult result,
                                 final List<CommandResult> subCommandsResult) {
        log.info(format(TABLE_FORMAT, "Times", repeat.getTimes()));
        result.put("Times", repeat.getTimes());
        for (int i = 0; i < repeat.getTimes(); i++) {
            this.repeatCommandsRunner.runCommands(repeat.getClickOrInputOrAssert(), dependencies, result,
                    subCommandsResult);
        }
    }
}
