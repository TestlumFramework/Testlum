package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.interpreter.OttUtil;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.UiOtt;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static java.lang.String.format;

@Slf4j
@ExecutorForClass(UiOtt.class)
public class OttExecutor extends AbstractUiExecutor<UiOtt> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String CODE_LOG = LogFormat.table("Generated code");
    private static final String ALIAS = "Alias";

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    private final OttUtil ottUtil;

    public OttExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.ottUtil = dependencies.getContext().getBean(OttUtil.class);
    }

    @Override
    protected void execute(final UiOtt uiOtt, final CommandResult result) {
        checkAlias(uiOtt);

        final String alias = uiOtt.getAlias();
        final String name = uiOtt.getName();

        final Supplier<String> codeSupplier = () -> {
            String code = ottUtil.generateCode(alias);
            log.info(CODE_LOG, code);
            return code;
        };

        if (uiOtt.isRefresh()) {
            dependencies.getScenarioContext().setLazyRefreshing(name, codeSupplier);
        } else {
            dependencies.getScenarioContext().setLazy(name, codeSupplier);
        }

        result.put(ALIAS, alias);
        log.info(ALIAS_LOG, alias);
    }

    private void checkAlias(final UiOtt uiOtt) {
        if (uiOtt.getAlias() == null) {
            uiOtt.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }
}
