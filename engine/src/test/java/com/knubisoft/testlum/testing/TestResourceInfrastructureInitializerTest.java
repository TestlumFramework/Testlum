package com.knubisoft.testlum.testing;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
class TestResourceInfrastructureInitializerTest {

    private TestResourceInfrastructureInitializer initializer;
    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        initializer = new TestResourceInfrastructureInitializer();
        GenericApplicationContext context = new GenericApplicationContext();
        initializer.initialize(context);
        beanFactory = context.getDefaultListableBeanFactory();
        beanFactory.getRegisteredScope("");
        context.getBeanFactoryPostProcessors().forEach(pp -> {
            if (pp
                    instanceof org.springframework.beans.factory.support
                    .BeanDefinitionRegistryPostProcessor registryPp) {
                registryPp.postProcessBeanDefinitionRegistry(beanFactory);
            }
        });
    }

    @Nested
    class Initialize {

        @Test
        void registersTestResourceSettingsBean() {
            assertTrue(beanFactory.containsBeanDefinition(
                    "testResourceSettings"));
            BeanDefinition bd = beanFactory.getBeanDefinition(
                    "testResourceSettings");
            assertEquals(TestResourceSettings.class.getName(),
                    bd.getBeanClassName());
            assertEquals(BeanDefinition.SCOPE_SINGLETON, bd.getScope());
        }

        @Test
        void registersFileSearcherBean() {
            assertTrue(beanFactory.containsBeanDefinition("fileSearcher"));
            BeanDefinition bd = beanFactory.getBeanDefinition("fileSearcher");
            assertEquals(FileSearcher.class.getName(),
                    bd.getBeanClassName());
            assertEquals(BeanDefinition.SCOPE_SINGLETON, bd.getScope());
        }

        @Test
        void registersXMLParsersBean() {
            assertTrue(beanFactory.containsBeanDefinition("XMLParsers"));
            BeanDefinition bd = beanFactory.getBeanDefinition("XMLParsers");
            assertEquals(XMLParsers.class.getName(),
                    bd.getBeanClassName());
            assertEquals(BeanDefinition.SCOPE_SINGLETON, bd.getScope());
        }

        @Test
        void fileSearcherHasConstructorAutowireMode() {
            BeanDefinition bd = beanFactory.getBeanDefinition("fileSearcher");
            assertInstanceOf(GenericBeanDefinition.class, bd);
            assertEquals(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR,
                    ((AbstractBeanDefinition) bd).getAutowireMode());
        }

        @Test
        void testResourceSettingsDoesNotHaveConstructorAutowire() {
            BeanDefinition bd = beanFactory.getBeanDefinition(
                    "testResourceSettings");
            assertInstanceOf(GenericBeanDefinition.class, bd);
            assertNotEquals(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR,
                    ((AbstractBeanDefinition) bd).getAutowireMode());
        }

        @Test
        void registersExactlyThreeBeans() {
            assertTrue(beanFactory.containsBeanDefinition(
                    "testResourceSettings"));
            assertTrue(beanFactory.containsBeanDefinition("fileSearcher"));
            assertTrue(beanFactory.containsBeanDefinition("XMLParsers"));
        }
    }
}
