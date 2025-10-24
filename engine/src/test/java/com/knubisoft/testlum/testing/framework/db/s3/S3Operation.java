package com.knubisoft.testlum.testing.framework.db.s3;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnS3EnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.S3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.Map;
import java.util.Objects;

@Conditional({OnS3EnabledCondition.class})
@Component
public class S3Operation extends AbstractStorageOperation {

    private final Map<AliasEnv, S3Client> s3Client;

    public S3Operation(@Autowired(required = false) final Map<AliasEnv, S3Client> s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String bucketName) {
        return null;
    }

    @Override
    public void clearSystem() {
        this.s3Client.forEach((aliasEnv, amazonS3) -> {
            if (isTruncate(S3.class, aliasEnv) && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                amazonS3.listBuckets().buckets().forEach(bucket -> {
                    ListObjectsV2Response objectsInBucket = amazonS3.listObjectsV2(builder -> builder.bucket(bucket.name()));
                    this.deleteObjectsInBucket(amazonS3, objectsInBucket, bucket.name());
                    amazonS3.deleteBucket(builder -> builder.bucket(bucket.name()));
                });
            }
        });
    }

    private void deleteObjectsInBucket(final S3Client amazonS3,
                                       final ListObjectsV2Response objectsInBucket,
                                       final String bucketName) {
        objectsInBucket.contents().forEach(object -> amazonS3
                .deleteObject(builder -> builder.bucket(bucketName).key(object.key())));
    }
}