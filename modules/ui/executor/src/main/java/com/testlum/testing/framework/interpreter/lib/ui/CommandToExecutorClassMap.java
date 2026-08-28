package com.testlum.testing.framework.interpreter.lib.ui;

import com.testlum.testing.model.scenario.AbstractUiCommand;

import java.util.LinkedHashMap;

public class CommandToExecutorClassMap
        extends LinkedHashMap
        <Class<? extends AbstractUiCommand>, Class<AbstractUiExecutor<? extends AbstractUiCommand>>> {

    private static final long serialVersionUID = 1;
}
