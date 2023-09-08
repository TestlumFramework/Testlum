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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@InterpreterForClass(Repeat.class)
public class RepeatInterpreter extends AbstractInterpreter<Repeat> {

    @Autowired
    private RepeatCommandsRunner repeatCommandsRunner;

    public RepeatInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Repeat o, final CommandResult result) {
        if (StringUtils.isNotBlank(o.getVariations())) {
            List<AbstractCommand> commands = o.getCommands();
            List<AbstractCommand> injectedCommand = GlobalVariations.getVariations(o.getVariations()).stream()
                    .flatMap(variation -> commands.stream()
                            .map(command -> InjectionUtil.injectObject(command, dependencies.getScenarioContext()))
                            .map(command -> InjectionUtil.injectObjectVariation(command, variation)))
                    .collect(Collectors.toList());
            this.repeatCommandsRunner.runCommands(injectedCommand, dependencies, result);
        } else {
            Repeat repeat = injectCommand(o);
            for (int i = 0; i < repeat.getTimes(); i++) {
                this.repeatCommandsRunner.runCommands(repeat.getCommands(), dependencies, result);
            }
        }
    }
}
