package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.WebRepeat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.REPEAT_FINISHED_LOG;

@Slf4j
@ExecutorForClass(WebRepeat.class)
public class WebRepeatExecutor extends AbstractUiExecutor<WebRepeat> {

    private final SubCommandRunner repeatCommandsRunner;
    private final GlobalVariations globalVariations;

    public WebRepeatExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        repeatCommandsRunner = dependencies.getContext().getBean(SubCommandRunner.class);
        globalVariations = dependencies.getContext().getBean(GlobalVariations.class);
    }

    @Override
    public void execute(final WebRepeat repeat, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        if (StringUtils.isNotBlank(repeat.getVariations())) {
            runRepeatWithVariations(repeat, result, subCommandsResult);
        } else {
            runSimpleRepeat(repeat, result, subCommandsResult);
        }
        log.info(REPEAT_FINISHED_LOG);
    }

    private void runRepeatWithVariations(final WebRepeat repeat,
                                         final CommandResult result,
                                         final List<CommandResult> subCommandsResult) {
        log.info(LogMessage.table("Variations", repeat.getVariations()));
        result.put("Variations", repeat.getVariations());
        List<AbstractUiCommand> commands = repeat.getClickOrInputOrAssert();
        List<AbstractUiCommand> injectedCommand = globalVariations.getVariations(repeat.getVariations()).stream()
                .flatMap(variation -> commands.stream().map(command ->
                        InjectionUtil.injectObjectVariation(command, variation, dependencies.getScenarioContext())))
                .collect(Collectors.toList());
        this.repeatCommandsRunner.runCommands(injectedCommand, dependencies, result, subCommandsResult);
    }

    private void runSimpleRepeat(final WebRepeat repeat,
                                 final CommandResult result,
                                 final List<CommandResult> subCommandsResult) {
        log.info(LogMessage.table("Times", String.valueOf(repeat.getTimes())));
        result.put("Times", repeat.getTimes());
        for (int i = 0; i < repeat.getTimes(); i++) {
            this.repeatCommandsRunner.runCommands(repeat.getClickOrInputOrAssert(), dependencies, result,
                    subCommandsResult);
        }
    }
}
