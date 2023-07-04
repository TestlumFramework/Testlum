package com.knubisoft.testlum.testing.framework.db.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
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
            if (isTruncate(Sqs.class, aliasEnv) && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                ListQueuesResult listQueuesResult = amazonSQS.listQueues();
                List<String> queueUrls = listQueuesResult.getQueueUrls();
                queueUrls.forEach(queueUrl -> amazonSQS.purgeQueue(new PurgeQueueRequest(queueUrl)));
            }
        });
    }
}
