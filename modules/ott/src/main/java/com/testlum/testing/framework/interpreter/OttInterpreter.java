package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Ott;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

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

        final String alias = ott.getAlias();
        final String name = ott.getName();

        final Supplier<String> codeSupplier = () -> {
            String code = ottUtil.generateCode(alias);
            log.info(CODE_LOG, code);
            return code;
        };

        if (ott.isRefresh()) {
            dependencies.getScenarioContext().setLazyRefreshing(name, codeSupplier);
        } else {
            dependencies.getScenarioContext().setLazy(name, codeSupplier);
        }

        result.put(ALIAS, alias);
        log.info(ALIAS_LOG, alias);
    }
}
