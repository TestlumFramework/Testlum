package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InjectionUtilImpl;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsImpl;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Repeat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@InterpreterForClass(Repeat.class)
public class RepeatInterpreter extends AbstractInterpreter<Repeat> {

    @Autowired
    private RepeatCommandRunner repeatCommandRunner;

    @Autowired
    private InjectionUtil injectionUtil;

    public RepeatInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Repeat o, final CommandResult result) {
        if (StringUtils.isNotBlank(o.getVariations())) {
            List<AbstractCommand> commands = o.getCommands();
            List<AbstractCommand> injectedCommand = GlobalVariationsImpl.getVariations(o.getVariations()).stream()
                    .flatMap(variation -> commands.stream().map(command ->
                            injectionUtil.injectObjectVariation(command, variation)))
                    .collect(Collectors.toList());
            this.repeatCommandRunner.runCommands(injectedCommand, dependencies, result);
        } else {
            Repeat repeat = injectCommand(o);
            for (int i = 0; i < repeat.getTimes(); i++) {
                this.repeatCommandRunner.runCommands(repeat.getCommands(), dependencies, result);
            }
        }
    }
}
