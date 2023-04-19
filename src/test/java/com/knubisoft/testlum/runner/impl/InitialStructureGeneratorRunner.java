package com.knubisoft.testlum.runner.impl;

import com.knubisoft.testlum.runner.Runner;
import com.knubisoft.testlum.testing.framework.util.ArgumentsUtils;
import com.knubisoft.testlum.testing.framework.util.InitialStructureGenerator;

public class InitialStructureGeneratorRunner implements Runner {

    @Override
    public void run(final String[] args) {
        InitialStructureGenerator.generate(ArgumentsUtils.getPathToInitialStructureGeneration(args[0]));
    }
}
