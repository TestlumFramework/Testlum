package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.WebRepeat;

import java.util.List;

@ExecutorForClass(WebRepeat.class)
public class WebRepeatExecutor extends AbstractRepeatExecutor<WebRepeat> {

    public WebRepeatExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected List<AbstractUiCommand> getCommands(final WebRepeat repeat) {
        return repeat.getClickOrInputOrAssert();
    }

    @Override
    protected Integer getTimes(final WebRepeat repeat) {
        return repeat.getTimes();
    }

    @Override
    protected String getVariations(final WebRepeat repeat) {
        return repeat.getVariations();
    }
}
