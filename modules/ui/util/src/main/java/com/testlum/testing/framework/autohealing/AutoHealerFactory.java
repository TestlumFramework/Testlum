package com.testlum.testing.framework.autohealing;

import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;

public interface AutoHealerFactory {

    AutoHealer create(ExecutorDependencies dependencies);
}
