package com.knubisoft.cott.testing.framework.db.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.knubisoft.cott.runner.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.model.AliasEnv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Conditional({OnS3EnabledCondition.class})
@Component
public class S3Operation implements StorageOperation {

    private final Map<AliasEnv, AmazonS3> amazonS3;

    public S3Operation(@Autowired(required = false) final Map<AliasEnv, AmazonS3> amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String bucketName) {
        return null;
    }

    @Override
    public void clearSystem() {
        this.amazonS3.forEach((aliasEnv, amazonS3) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.getThreadEnv())) {
                String bucketName = aliasEnv.getAlias();
                ListObjectsV2Result objectsInBucket = amazonS3.listObjectsV2(bucketName);
                this.deleteObjectsInBucket(amazonS3, objectsInBucket, bucketName);
            }
        });
    }

    private void deleteObjectsInBucket(final AmazonS3 amazonS3,
                                       final ListObjectsV2Result objectsInBucket,
                                       final String bucketName) {
        for (final S3ObjectSummary objectSummary : objectsInBucket.getObjectSummaries()) {
            amazonS3.deleteObject(bucketName, objectSummary.getKey());
        }
    }
}
