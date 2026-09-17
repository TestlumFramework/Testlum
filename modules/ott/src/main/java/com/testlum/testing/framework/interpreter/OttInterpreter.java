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

    private final OttGenerator ottGenerator;

    public OttInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.ottGenerator = dependencies.getContext().getBean(OttGenerator.class);
    }

    @Override
    protected void acceptImpl(final Ott o, final CommandResult result) {
        Ott ott = injectCommand(o);
        ensureAlias(ott::getAlias, ott::setAlias);

        final String alias = ott.getAlias();

        storeCode(ott, buildCodeSupplier(alias, ott.getSecret()));

        result.put(ALIAS, alias);
        log.info(ALIAS_LOG, alias);
    }

    private Supplier<String> buildCodeSupplier(final String alias, final String secret) {
        return () -> {
            String code = secret != null
                    ? ottGenerator.generateCodeFromSecret(secret)
                    : ottGenerator.generateCode(alias);
            log.info(CODE_LOG, code);
            return code;
        };
    }

    private void storeCode(final Ott ott, final Supplier<String> codeSupplier) {
        final String name = ott.getName();

        if (Boolean.TRUE.equals(ott.isRefresh())) {
            dependencies.getScenarioContext().setLazyRefreshing(name, codeSupplier);
        } else {
            dependencies.getScenarioContext().setLazy(name, codeSupplier);
        }
    }
}
