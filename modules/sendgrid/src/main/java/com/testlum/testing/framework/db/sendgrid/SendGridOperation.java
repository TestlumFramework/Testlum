package com.testlum.testing.framework.db.sendgrid;

import com.testlum.testing.framework.db.AbstractStorageOperation;
import com.testlum.testing.framework.db.source.Source;
import org.springframework.stereotype.Component;

@Component
public class SendGridOperation extends AbstractStorageOperation {

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
    }
}
