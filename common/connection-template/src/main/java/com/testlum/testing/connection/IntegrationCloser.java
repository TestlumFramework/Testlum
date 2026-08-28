package com.testlum.testing.connection;

@FunctionalInterface
public interface IntegrationCloser<T> {

    void close(T integration) throws Exception;

}
