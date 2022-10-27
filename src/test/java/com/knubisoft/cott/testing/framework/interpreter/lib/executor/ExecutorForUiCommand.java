package com.knubisoft.cott.testing.framework.interpreter.lib.executor;

import com.knubisoft.cott.testing.model.scenario.AbstractCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutorForUiCommand { //or Executor, ExecutorForClass, SeleniumCommandExecutor, SeleniumExecutor
    // UiCommandExecutor

    Class<? extends AbstractCommand> value(); //тут нужен будет extends другого типа,
    // возможно в xsd создать класс по типу AbstractUiCommand или UiCommand
}
