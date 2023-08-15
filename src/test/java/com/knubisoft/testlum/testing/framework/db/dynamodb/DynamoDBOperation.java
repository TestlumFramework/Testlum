package com.knubisoft.testlum.testing.framework.db.dynamodb;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
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
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
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
@Component("dynamoOperation")
public class DynamoDBOperation implements StorageOperation {

    private static final String FORBIDDEN_DDB_TABLE_NAME = "dynamobee";

    private final Map<AliasEnv, DynamoDbClient> dynamoDbClient;
    @Autowired
    private GlobalTestConfigurationProvider configurationProvider;
    @Autowired
    private EnvManager envManager;

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
            if (isTruncate(Dynamo.class, aliasEnv, configurationProvider)
                    && Objects.equals(aliasEnv.getEnvironment(), envManager.currentEnv())) {
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
        ExecuteStatementResponse singleResponse = dynamoDbClient.get(new AliasEnv(alias, envManager.currentEnv()))
                .executeStatement(builder -> builder.statement(query));
        return new QueryResult<>(query, singleResponse.items());
    }

    private void truncate(final String tableName, final DynamoDbClient dbClient) {
        if (!FORBIDDEN_DDB_TABLE_NAME.equals(tableName)) {
            ScanIterable scanIterable = dbClient.scanPaginator(ScanRequest.builder().tableName(tableName).build());
            DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName).build();
            DescribeTableResponse describeTable = dbClient.describeTable(tableRequest);
            truncateTable(scanIterable, tableName, describeTable, dbClient);
        }
    }

    private void truncateTable(final ScanIterable scanIterable,
                               final String tableName,
                               final DescribeTableResponse describeTable,
                               final DynamoDbClient dbClient) {
        for (ScanResponse scanResponse : scanIterable) {
            scanResponse.items().forEach(item -> deleteItem(tableName, describeTable, item, dbClient));
        }
    }

    private void deleteItem(final String tableName,
                            final DescribeTableResponse describeTable,
                            final Map<String, AttributeValue> item,
                            final DynamoDbClient dbClient) {
        Map<String, AttributeValue> deleteKey = new HashMap<>();
        for (KeySchemaElement keySchemaElement : describeTable.table().keySchema()) {
            String attributeName = keySchemaElement.attributeName();
            deleteKey.put(attributeName, item.get(attributeName));
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
