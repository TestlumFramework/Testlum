package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.invoke.AbstractInvokeSupplier;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.model.scenario.Invoke;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.FUNCTION_IS_NOT_INVOKE_SUPPLIER;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.UNKNOWN_OPERATION;

@Slf4j
@InterpreterForClass(Invoke.class)
public class InvokeInterpreter extends AbstractInterpreter<Invoke> {

    public InvokeInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Invoke o, final CommandResult result) {
        if (o.getClassFunction() != null) {
            result.put("Function", o.getClassFunction());
            acceptClassFunction(o, result);
        } else if (o.getTarget() != null) {
            throw new UnsupportedOperationException("TODO");
        } else {
            throw new DefaultFrameworkException(UNKNOWN_OPERATION);
        }
    }

    @SneakyThrows
    private void acceptClassFunction(final Invoke o, final CommandResult result) {
        Class<?> cls = Class.forName(o.getClassFunction());
        Object instance = cls.getDeclaredConstructor().newInstance();
        if (instance instanceof AbstractInvokeSupplier) {
            invokeOnInstance(o, instance, result);
        } else {
            throw new DefaultFrameworkException(FUNCTION_IS_NOT_INVOKE_SUPPLIER);
        }
    }

    //CHECKSTYLE:OFF
    private void invokeOnInstance(final Invoke o, final Object instance, final CommandResult result) {
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(instance);
        String actual = toString(((Supplier<?>) instance).get());
        CompareBuilder compare = newCompare()
                .withActual(actual)
                .withExpectedFile(o.getFile());
        result.setActual(PrettifyStringJson.getJSONResult(actual));
        result.setExpected(PrettifyStringJson.getJSONResult(compare.getExpected()));
        compare.exec();
        setContextBody(actual);
    }
    //CHECKSTYLE:ON
}
