package com.knubisoft.cott.testing.framework.db.dynamodb;

import com.knubisoft.cott.runner.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.model.AliasEnv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Conditional({OnDynamoEnabledCondition.class})
@Component
public class DynamoDBOperation implements StorageOperation {

    private static final String FORBIDDEN_DDB_TABLE_NAME = "dynamobee";

    private final Map<AliasEnv, DynamoDbClient> dynamoDbClient;

    public DynamoDBOperation(@Autowired(required = false)
                             final Map<AliasEnv, DynamoDbClient> dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        return new StorageOperationResult(execute(source.getQueries(), databaseAlias));
    }

    @Override
    public void clearSystem() {
        dynamoDbClient.forEach((aliasEnv, dbClient) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.getThreadEnv())) {
                dbClient.listTables().tableNames().forEach(tableName -> truncate(tableName, dbClient));
            }
        });
    }

    private List<QueryResult<List<Map<String, AttributeValue>>>> execute(final List<String> statements,
                                                                         final String alias) {
        return statements.stream().map(s -> executeSingleQuery(s, alias)).collect(Collectors.toList());
    }

    private QueryResult<List<Map<String, AttributeValue>>> executeSingleQuery(final String query,
                                                                              final String alias) {
        ExecuteStatementResponse singleResponse = dynamoDbClient.get(AliasEnv.build(alias))
                .executeStatement(builder -> builder.statement(query));
        return new QueryResult<>(query, singleResponse.items());
    }

    private void truncate(final String tableName, final DynamoDbClient dbClient) {
        if (!FORBIDDEN_DDB_TABLE_NAME.equals(tableName)) {
            ScanIterable scanIterable = dbClient.scanPaginator(ScanRequest.builder().tableName(tableName).build());
            DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName).build();
            DescribeTableResponse describeTableResponse = dbClient.describeTable(tableRequest);
            String hashKey = describeTableResponse.table().keySchema().get(0).attributeName();
            String rangeKey = describeTableResponse.table().keySchema().get(1).attributeName();
            truncate(scanIterable, tableName, hashKey, rangeKey, dbClient);
        }
    }

    private void truncate(final ScanIterable scanIterable,
                          final String tableName,
                          final String hashKey,
                          final String rangeKey,
                          final DynamoDbClient dbClient) {
        for (ScanResponse scanResponse : scanIterable) {
            scanResponse.items().forEach(item -> deleteItem(tableName, hashKey, rangeKey, item, dbClient));
        }
    }

    private void deleteItem(final String tableName,
                            final String hashKey,
                            final String rangeKey,
                            final Map<String, AttributeValue> item,
                            final DynamoDbClient dbClient) {
        Map<String, AttributeValue> deleteKey = new HashMap<>();
        deleteKey.put(hashKey, item.get(hashKey));
        if (rangeKey != null) {
            deleteKey.put(rangeKey, item.get(rangeKey));
        }
        deleteItemByTableNameAndKey(tableName, deleteKey, dbClient);
    }

    private void deleteItemByTableNameAndKey(final String tableName,
                                             final Map<String, AttributeValue> deleteKey,
                                             final DynamoDbClient dbClient) {
        dbClient.deleteItem(DeleteItemRequest
                .builder()
                .tableName(tableName)
                .key(deleteKey)
                .build());
    }
}
