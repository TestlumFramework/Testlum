package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.wait.util.WaitUtil;
import com.testlum.testing.model.scenario.WaitNative;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@ExecutorForClass(WaitNative.class)
@Slf4j
public class WaitNativeExecutor extends AbstractUiExecutor<WaitNative> {

    private final WaitUtil waitUtil;

    public WaitNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.waitUtil = dependencies.getContext().getBean(WaitUtil.class);
    }

    @Override
    protected void execute(final WaitNative wait, final CommandResult result) {
        String time = wait.getTime();
        log.info(LogMessage.WAIT_INFO_LOG, time, wait.getUnit());
        TimeUnit timeUnit = waitUtil.getTimeUnit(wait.getUnit());
        resultUtil.addWaitMetaData(time, timeUnit, result);
        waitUtil.sleep(Long.parseLong(time), timeUnit);
    }
}
