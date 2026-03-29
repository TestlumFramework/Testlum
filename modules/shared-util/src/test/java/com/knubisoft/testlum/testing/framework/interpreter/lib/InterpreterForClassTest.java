package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InterpreterForClassTest {

    @Test
    void retentionIsRuntime() {
        assertEquals(RetentionPolicy.RUNTIME,
                InterpreterForClass.class.getAnnotation(java.lang.annotation.Retention.class).value());
    }

    @Test
    void targetIsType() {
        java.lang.annotation.Target target =
                InterpreterForClass.class.getAnnotation(java.lang.annotation.Target.class);
        assertNotNull(target);
        assertEquals(1, target.value().length);
        assertEquals(ElementType.TYPE, target.value()[0]);
    }

    @Test
    void annotationCanBeAppliedAndRead() {
        InterpreterForClass annotation = AnnotatedClass.class.getAnnotation(InterpreterForClass.class);
        assertNotNull(annotation);
        assertEquals(TestCommand.class, annotation.value());
    }

    @InterpreterForClass(TestCommand.class)
    private static class AnnotatedClass {
    }

    private static class TestCommand extends AbstractCommand {
    }
}
