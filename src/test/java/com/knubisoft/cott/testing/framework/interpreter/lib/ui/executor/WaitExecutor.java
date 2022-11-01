package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.WaitUtil;
import com.knubisoft.cott.testing.model.scenario.WaitUi;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.TIME;

@ExecutorForClass(WaitUi.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<WaitUi> {

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @SneakyThrows
    @Override
    public void execute(final WaitUi waitUi, final CommandResult result) {
        String time = inject(waitUi.getTime());
        result.put(TIME, time);
        log.info(WAIT_INFO_LOG, time, waitUi.getUnit());
        WaitUtil.getTimeUnit(waitUi.getUnit(), result).sleep(Long.parseLong(time));
    }
}
