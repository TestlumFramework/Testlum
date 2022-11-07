package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.JavascriptUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Scroll;

@ExecutorForClass(Scroll.class)
public class ScrollWebExecutor extends AbstractUiExecutor<Scroll> {

    public ScrollWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Scroll scroll, final CommandResult result) {
        ResultUtil.addScrollMetaData(scroll, result);
        LogUtil.logScrollInfo(scroll);
        JavascriptUtil.executeScrollScript(scroll, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
