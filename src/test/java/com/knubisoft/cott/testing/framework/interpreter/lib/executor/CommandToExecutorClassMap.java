package com.knubisoft.cott.testing.framework.interpreter.lib.executor;

import com.knubisoft.cott.testing.model.scenario.AbstractCommand;

import java.util.LinkedHashMap;

public class CommandToExecutorClassMap
        extends LinkedHashMap
        <Class<? extends AbstractCommand>, UiCommandExecutor<? extends AbstractCommand>> {

    private static final long serialVersionUID = 1;
}
