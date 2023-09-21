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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@InterpreterForClass(Repeat.class)
public class RepeatInterpreter extends AbstractInterpreter<Repeat> {

    private final RepeatCommandRunner repeatCommandsRunner;
    private final GlobalVariations globalVariations;

    public RepeatInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        repeatCommandsRunner = dependencies.getContext().getBean(RepeatCommandRunner.class);
        globalVariations = dependencies.getContext().getBean(GlobalVariations.class);
    }

    @Override
    protected void acceptImpl(final Repeat o, final CommandResult result) {
        if (StringUtils.isNotBlank(o.getVariations())) {
            List<AbstractCommand> commands = o.getCommands();
            List<AbstractCommand> injectedCommand = globalVariations.getVariations(o.getVariations()).stream()
                    .flatMap(variation -> commands.stream().map(command ->
                            injectObjectVariation(command, variation)))
                    .collect(Collectors.toList());
            this.repeatCommandsRunner.runCommands(injectedCommand, dependencies, result);
        } else {
            Repeat repeat = injectCommand(o);
            for (int i = 0; i < repeat.getTimes(); i++) {
                this.repeatCommandsRunner.runCommands(repeat.getCommands(), dependencies, result);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = globalVariations.getVariationValue(asJson, variation);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
