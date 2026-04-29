package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Javascript;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExecutorForClass(Javascript.class)
public class JavascriptExecutor extends AbstractUiExecutor<Javascript> {

    public JavascriptExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Javascript javascript, final CommandResult result) {
        String fileName = javascript.getFile();
        result.put(ResultUtil.JS_FILE, fileName);
        log.info(LogMessage.JS_FILE_LOG, fileName);
        String command = javascriptUtil.readCommands(fileName);
        javascriptUtil.executeJsScript(command, dependencies.getDriver());
    }
}
