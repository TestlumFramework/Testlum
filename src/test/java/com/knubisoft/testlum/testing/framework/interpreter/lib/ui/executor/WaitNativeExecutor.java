package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.WaitNative;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;

@ExecutorForClass(WaitNative.class)
@Slf4j
public class WaitNativeExecutor extends AbstractUiExecutor<WaitNative> {

    public WaitNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final WaitNative wait, final CommandResult result) {
        String time = wait.getTime();
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        TimeUnit timeUnit = WaitUtil.getTimeUnit(wait.getUnit());
        ResultUtil.addWaitMetaData(time, timeUnit, result);
        WaitUtil.sleep(Long.parseLong(time), timeUnit);
    }
}
