package com.knubisoft.cott.testing.framework.interpreter;

import com.google.gson.Gson;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.GraphQlUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.model.scenario.Graphql;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@InterpreterForClass(Graphql.class)
public class GraphQLInterpreter extends AbstractInterpreter<Graphql> {


    public static final int VALID_RESPONSE = 200;

    public GraphQLInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @SneakyThrows
    @Override
    public void acceptImpl(final Graphql graphql, final CommandResult result) {
        String actual = getActual(graphql);
        CompareBuilder compare = newCompare()
                .withActual(actual)
                .withExpectedFile(graphql.getFile());
        compare.exec();
        result.setActual(PrettifyStringJson.getJSONResult(actual));
        result.setExpected(PrettifyStringJson.getJSONResult(compare.getExpected()));
    }

    @SneakyThrows
    private String getActual(final Graphql graphql) {
        CloseableHttpClient client = HttpClients.createDefault();
        Gson gson = new Gson();
        GraphQlUtil graphQlUtil = new GraphQlUtil();
        String actual = null;
        for (String each : graphql.getQuery()) {
            graphQlUtil.setQuery(each);
            String jsonQuery = gson.toJson(graphQlUtil);
            HttpResponse response = getResponse(graphql, client, jsonQuery);
            actual = EntityUtils.toString(response.getEntity());
        }
        return actual;
    }

    @SneakyThrows
    private static HttpResponse getResponse(final Graphql graphql,
                                            final CloseableHttpClient client,
                                            final String jsonQuery) {
        HttpPost post = new HttpPost(graphql.getEndpoint());
        post.setEntity(new StringEntity(jsonQuery, ContentType.APPLICATION_JSON));
        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != VALID_RESPONSE) {
            throw new DefaultFrameworkException("Query execution failed, response code: %S",
                    response.getStatusLine().getStatusCode());
        }
        return response;
    }
}
