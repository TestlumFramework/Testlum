package com.knubisoft.testlum.testing.framework.env.service;

public interface LockService {

    void runLocked(TaskCallback taskCallback);


    interface TaskCallback {
        void execute();
    }
}
