package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor.ClickExecutor;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.Click;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorForClassTest {

    @Nested
    class AnnotationMetadata {

        @Test
        void hasRuntimeRetention() {
            Retention retention = ExecutorForClass.class.getAnnotation(Retention.class);
            assertNotNull(retention);
            assertEquals(RetentionPolicy.RUNTIME, retention.value());
        }

        @Test
        void targetsTypeLevel() {
            Target target = ExecutorForClass.class.getAnnotation(Target.class);
            assertNotNull(target);
            assertEquals(1, target.value().length);
            assertEquals(ElementType.TYPE, target.value()[0]);
        }

        @Test
        void isAnnotation() {
            assertTrue(ExecutorForClass.class.isAnnotation());
        }
    }

    @Nested
    class Usage {

        @Test
        void clickExecutorIsAnnotatedWithClickClass() {
            ExecutorForClass annotation = ClickExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertEquals(Click.class, annotation.value());
        }

        @Test
        void valueReturnsAbstractUiCommandSubclass() {
            ExecutorForClass annotation = ClickExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertTrue(AbstractUiCommand.class.isAssignableFrom(annotation.value()));
        }
    }
}
