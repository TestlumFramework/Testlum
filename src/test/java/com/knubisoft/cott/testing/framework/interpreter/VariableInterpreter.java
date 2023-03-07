package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.MockDriver;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.VarImplementation;
import com.knubisoft.cott.testing.model.scenario.GeneralVar;
import com.knubisoft.cott.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;

@Slf4j
@InterpreterForClass(GeneralVar.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
    @Autowired(required = false)
    private NameToAdapterAlias nameToAdapterAlias;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Var var, final CommandResult result) {
        try {
            VarImplementation varImpl = new VarImplementation(
                    dependencies.getScenarioContext(),
                    new MockDriver("Web is not allowed here"),
                    dependencies.getFile());
            varImpl.setContextVariable(var, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_WITH_PATH_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

}
