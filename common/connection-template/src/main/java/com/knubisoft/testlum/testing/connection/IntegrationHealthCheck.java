package com.knubisoft.testlum.testing.connection;

@FunctionalInterface
public interface IntegrationHealthCheck<T> {

    void verify(T integration) throws Exception;

}
