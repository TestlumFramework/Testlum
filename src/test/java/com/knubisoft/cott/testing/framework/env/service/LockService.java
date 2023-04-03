package com.knubisoft.cott.testing.framework.env.service;

public interface LockService {

    void runLocked(TaskCallback taskCallback);


    interface TaskCallback {
        void execute();
    }
}
