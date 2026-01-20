package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;

import java.io.Serial;
import java.util.LinkedHashMap;

public class CommandToExecutorClassMap
        extends LinkedHashMap
        <Class<? extends AbstractUiCommand>, Class<AbstractUiExecutor<? extends AbstractUiCommand>>> {

    @Serial
    private static final long serialVersionUID = 1;
}
