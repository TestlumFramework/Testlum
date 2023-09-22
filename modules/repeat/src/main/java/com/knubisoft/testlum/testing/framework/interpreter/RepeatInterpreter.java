package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.RepeatCommandRunner;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Repeat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Repeat.class)
public class RepeatInterpreter extends AbstractInterpreter<Repeat> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String COMMAND_REPEAT_FINISHED_LOG = ANSI_YELLOW + "------- Repeat is finished -------"
            + ANSI_RESET;

    private final RepeatCommandRunner repeatCommandsRunner;
    private final GlobalVariations globalVariations;

    public RepeatInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        repeatCommandsRunner = dependencies.getContext().getBean(RepeatCommandRunner.class);
        globalVariations = dependencies.getContext().getBean(GlobalVariations.class);
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
        List<AbstractCommand> injectedCommand = globalVariations.getVariations(repeat.getVariations()).stream()
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

    @SuppressWarnings("unchecked")
    private <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = globalVariations.getValue(asJson, variation);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
