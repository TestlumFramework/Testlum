package com.knubisoft.cott.testing.framework.interpreter.lib.executor;


import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import org.openqa.selenium.WebDriver;

public interface UiCommandExecutor<T extends AbstractCommand> /*(тут тоже нужен extends другого типа).
  так же можно подумать над другим названием Executor, CommandExecutor, UiExecutor*/ {

    void execute(T t, WebDriver driver, CommandResult result);
}
