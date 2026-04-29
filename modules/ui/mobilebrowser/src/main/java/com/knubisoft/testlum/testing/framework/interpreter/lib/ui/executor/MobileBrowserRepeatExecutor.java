package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.MobilebrowserRepeat;

import java.util.List;

@ExecutorForClass(MobilebrowserRepeat.class)
public class MobileBrowserRepeatExecutor extends AbstractRepeatExecutor<MobilebrowserRepeat> {

    public MobileBrowserRepeatExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected List<AbstractUiCommand> getCommands(final MobilebrowserRepeat repeat) {
        return repeat.getClickOrInputOrAssert();
    }

    @Override
    protected Integer getTimes(final MobilebrowserRepeat repeat) {
        return repeat.getTimes();
    }

    @Override
    protected String getVariations(final MobilebrowserRepeat repeat) {
        return repeat.getVariations();
    }
}
