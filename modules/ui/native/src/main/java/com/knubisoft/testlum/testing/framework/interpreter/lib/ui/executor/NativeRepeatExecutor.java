package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.NativeRepeat;

import java.util.List;

@ExecutorForClass(NativeRepeat.class)
public class NativeRepeatExecutor extends AbstractRepeatExecutor<NativeRepeat> {

    public NativeRepeatExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected List<AbstractUiCommand> getCommands(final NativeRepeat repeat) {
        return repeat.getClickOrInputOrAssert();
    }

    @Override
    protected Integer getTimes(final NativeRepeat repeat) {
        return repeat.getTimes();
    }

    @Override
    protected String getVariations(final NativeRepeat repeat) {
        return repeat.getVariations();
    }
}
