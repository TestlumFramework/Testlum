package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;

import java.io.Serial;
import java.util.LinkedHashMap;

public class CommandToInterpreterClassMap
        extends LinkedHashMap
        <Class<? extends AbstractCommand>, Class<AbstractInterpreter<? extends AbstractCommand>>> {

    @Serial
    private static final long serialVersionUID = 1;
}
