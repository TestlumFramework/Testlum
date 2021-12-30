package com.knubisoft.e2e.testing.framework.interpreter.lib;

import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;

import java.util.LinkedHashMap;

public class CommandToInterpreterClassMap
        extends LinkedHashMap
        <Class<? extends AbstractCommand>, Class<AbstractInterpreter<? extends AbstractCommand>>> {

    private static final long serialVersionUID = 1;
}
