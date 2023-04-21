package com.knubisoft.testlum.testing.framework.db.ses;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import org.springframework.stereotype.Component;

@Component
public class SESOperation implements StorageOperation {

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
    }
}
