package com.knubisoft.cott.testing.framework.interpreter.lib.executor;

import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.web.scenario.CommandOne;
import org.openqa.selenium.WebDriver;

@ExecutorForUiCommand(CommandOne.class)
public class CommandOneExecutor implements UiCommandExecutor<CommandOne> {

    @Override
    public void execute(CommandOne commandOne, WebDriver driver, CommandResult result) {
        //execute logic
    }
}
