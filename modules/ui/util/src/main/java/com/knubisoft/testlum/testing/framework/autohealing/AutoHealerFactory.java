package com.knubisoft.testlum.testing.framework.autohealing;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;

public interface AutoHealerFactory {

    AutoHealer create(ExecutorDependencies dependencies);
}
