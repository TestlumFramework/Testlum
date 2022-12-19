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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        GraphQlUtil graphQlUtil = new GraphQlUtil();
        String actual = null;
        for (String each : graphql.getQuery()) {
            graphQlUtil.setQuery(each);
            String jsonQuery = gson.toJson(graphQlUtil);
            Response response = getResponse(graphql, client, jsonQuery);
            actual = response.body().string();
        }
        return actual;
    }

    @SneakyThrows
    private static Response getResponse(final Graphql graphql,
                                        final OkHttpClient client,
                                        final String jsonQuery) {
        Request post = new Request.Builder()
                .url(graphql.getEndpoint())
                .post(RequestBody.create(jsonQuery,
                        MediaType.parse("application/json")))
                .build();
        Response response = client.newCall(post).execute();
        if (response.code() != VALID_RESPONSE) {
            throw new DefaultFrameworkException("Query execution failed, response code: %S", response.code());
        }
        return response;
    }
}
