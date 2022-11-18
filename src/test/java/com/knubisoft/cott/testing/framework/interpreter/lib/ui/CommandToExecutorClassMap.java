package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;

import java.util.LinkedHashMap;

public class CommandToExecutorClassMap
    extends LinkedHashMap
        <Class<? extends AbstractUiCommand>, Class<? extends AbstractUiExecutor<AbstractUiCommand>>> {

        private static final long serialVersionUID = 1;
}
