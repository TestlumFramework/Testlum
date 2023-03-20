package com.knubisoft.cott.testing.framework.db.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.env.AliasEnv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Conditional({OnSQSEnabledCondition.class})
@Component
public class SQSOperation implements StorageOperation {

    private final Map<AliasEnv, AmazonSQS> amazonSQS;

    public SQSOperation(@Autowired(required = false) final Map<AliasEnv, AmazonSQS> amazonSQS) {
        this.amazonSQS = amazonSQS;
    }

    @Override
    public StorageOperation.StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
        amazonSQS.forEach((aliasEnv, amazonSQS) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                ListQueuesResult listQueuesResult = amazonSQS.listQueues();
                List<String> queueUrls = listQueuesResult.getQueueUrls();
                queueUrls.forEach(amazonSQS::deleteQueue);
            }
        });
    }
}
