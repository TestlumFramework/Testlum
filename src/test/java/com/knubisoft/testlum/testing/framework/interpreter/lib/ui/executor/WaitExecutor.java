package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Wait;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;

@ExecutorForClass(Wait.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<Wait> {

    @Autowired
    private WaitUtil waitUtil;

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Wait wait, final CommandResult result) {
        String time = wait.getTime();
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        TimeUnit timeUnit = waitUtil.getTimeUnit(wait.getUnit());
        ResultUtil.addWaitMetaData(time, timeUnit, result);
        waitUtil.sleep(Long.parseLong(time), timeUnit);
    }
}
