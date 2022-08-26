package com.knubisoft.cott.runner.impl;

import com.knubisoft.cott.runner.Runner;
import com.knubisoft.cott.testing.framework.util.ArgumentsUtils;
import com.knubisoft.cott.testing.framework.util.InitialStructureGenerator;

public class InitialStructureGeneratorRunner implements Runner {

    @Override
    public void run(final String[] args) {
        new InitialStructureGenerator().generate(ArgumentsUtils.getPathToInitialStructureGeneration(args[0]));
    }
}
