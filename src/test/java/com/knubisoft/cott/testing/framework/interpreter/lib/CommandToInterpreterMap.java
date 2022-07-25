package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.model.scenario.AbstractCommand;

import java.util.LinkedHashMap;

public class CommandToInterpreterMap
        extends LinkedHashMap
        <Class<? extends AbstractCommand>, AbstractInterpreter<? extends AbstractCommand>> {

    private static final long serialVersionUID = 1;
}
