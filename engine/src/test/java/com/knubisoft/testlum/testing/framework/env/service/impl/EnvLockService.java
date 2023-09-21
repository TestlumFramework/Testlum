package com.knubisoft.testlum.testing.framework.env.service.impl;

import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.env.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvLockService implements LockService {

    @Autowired
    private final EnvManager envManager;

    @SneakyThrows
    @Override
    public final void runLocked(final TaskCallback taskCallback) {
        String env = envManager.acquireEnv();
        try {
            taskCallback.execute();
        } finally {
            envManager.releaseEnv(env);
        }
    }
}
