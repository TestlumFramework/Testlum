package com.knubisoft.e2e.testing.framework.db.sendgrid;

import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import org.springframework.stereotype.Component;

@Component
public class SendGridOperation implements StorageOperation {

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
    }
}
