package com.knubisoft.testlum.testing.framework.configuration.connection.health;

@FunctionalInterface
public interface IntegrationHealthCheck<T> {

    void verify(T integration) throws Exception;

}
