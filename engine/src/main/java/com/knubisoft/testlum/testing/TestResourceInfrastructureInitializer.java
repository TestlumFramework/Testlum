package com.knubisoft.testlum.testing;

import com.knubisoft.testlum.starter.summary.TestExecutionPostProcessor;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class TestResourceInfrastructureInitializer
        implements ApplicationContextInitializer<GenericApplicationContext> {

    @Override
    public void initialize(final GenericApplicationContext context) {
        context.addBeanFactoryPostProcessor((BeanDefinitionRegistryPostProcessor) registry -> {
            registerBeanDefinition(registry, "testResourceSettings", TestResourceSettings.class, false);
            registerBeanDefinition(registry, "fileSearcher", FileSearcher.class, true);
            registerBeanDefinition(registry, "XMLParsers", XMLParsers.class, false);
            registerBeanDefinition(registry, "testExecutionPostProcessor", TestExecutionPostProcessor.class, false);
        });
    }

    private static void registerBeanDefinition(final BeanDefinitionRegistry registry,
                                               final String beanName,
                                               final Class<?> beanClass,
                                               final boolean hasAdditionDependenciesInConstructor) {
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(beanClass);
        genericBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (hasAdditionDependenciesInConstructor) {
            genericBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        }

        registry.registerBeanDefinition(beanName, genericBeanDefinition);
    }
}
