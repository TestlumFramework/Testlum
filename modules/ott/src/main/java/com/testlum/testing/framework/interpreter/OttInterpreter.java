package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Ott;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Ott.class)
public class OttInterpreter extends AbstractInterpreter<Ott> {

    private static final String CODE_LOG = LogFormat.table("Generated code");

    private final OttUtil ottUtil;

    public OttInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.ottUtil = dependencies.getContext().getBean(OttUtil.class);
    }

    @Override
    protected void acceptImpl(final Ott o, final CommandResult result) {
        Ott ott = injectCommand(o);
        ensureAlias(ott::getAlias, ott::setAlias);
        String code = ottUtil.generateCode(ott.getAlias());
        dependencies.getScenarioContext().set(ott.getName(), code);
        addOttMetaData(ott.getAlias(), code, result);
    }

    private void addOttMetaData(final String alias, final String code, final CommandResult result) {
        result.put(ALIAS, alias);
        log.info(ALIAS_LOG, alias);
        log.info(CODE_LOG, code);
    }
}
