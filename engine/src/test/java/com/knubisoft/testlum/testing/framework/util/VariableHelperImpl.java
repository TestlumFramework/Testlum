package com.knubisoft.testlum.testing.framework.util;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.global_config.GoogleAuth;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.xml.sax.InputSource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.DOLLAR_SIGN;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SLASH_SEPARATOR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.FILE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.GENERATED_STRING;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.XML_PATH;
import static java.util.Objects.nonNull;

@Slf4j
@Component
public class VariableHelperImpl implements VariableHelper {

	private static final String HMAC_ALGORITHM = "HmacSHA1";
	private static final String SIX_DIGITS_FORMAT = "%06d";

    private final Map<RandomPredicate, RandomFunction> randomGenerateMethodMap;
    @Autowired
    private AliasToStorageOperation aliasToStorageOperation;

    public VariableHelperImpl() {
        randomGenerateMethodMap = Map.of(
                r -> nonNull(r.getNumeric()), r -> RandomStringUtils.randomNumeric(r.getLength()),
                r -> nonNull(r.getAlphabetic()), r -> RandomStringUtils.randomAlphabetic(r.getLength()),
                r -> nonNull(r.getAlphanumeric()), r -> RandomStringUtils.randomAlphanumeric(r.getLength()),
                r -> nonNull(r.getRandomRegexp()), this::generateStringByRegexp);
    }

    @Override
    public <T extends AbstractCommand> VarMethod<T> lookupVarMethod(final Map<VarPredicate<T>, VarMethod<T>> methodMap,
                                                                    final T var) {
        return methodMap.entrySet().stream()
                .filter(entry -> entry.getKey().test(var))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.VAR_TYPE_NOT_SUPPORTED,
                        var.getClass().getSimpleName()))
                .getValue();
    }

    @Override
    public Supplier<String> getRandomGenerateResult(final FromRandomGenerate randomGenerate,
                                          final String varName,
                                          final CommandResult result) {
        String valueResult = getRandomGeneratedString(randomGenerate);
        String exp = nonNull(randomGenerate.getRandomRegexp())
                ? randomGenerate.getRandomRegexp().getPattern() : NO_EXPRESSION;
        ResultUtil.addVariableMetaData(GENERATED_STRING, varName, exp, valueResult, result);
        return () -> valueResult;
    }

    private String getRandomGeneratedString(final FromRandomGenerate randomGenerate) {
        return randomGenerateMethodMap.entrySet().stream()
                .filter(e -> e.getKey().test(randomGenerate))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.GENERATION_METHOD_NOT_SUPPORTED))
                .getValue().apply(randomGenerate);
    }

    private String generateStringByRegexp(final FromRandomGenerate randomGenerate) {
        RgxGen rgxGen = new RgxGen(randomGenerate.getRandomRegexp().getPattern());
        int requiredLength = randomGenerate.getLength();
        StringBuilder randomString = new StringBuilder();
        while (randomString.length() < requiredLength) {
            randomString.append(rgxGen.generate());
        }
        if (randomString.length() > requiredLength) {
            randomString.delete(requiredLength, randomString.length());
        }
        return randomString.toString();
    }

    @Override
    public Supplier<String> getFileResult(final FromFile fromFile,
                                final String varName,
                                final UnaryOperator<String> fileToString,
                                final CommandResult result) {
        String valueResult = fileToString.apply(fromFile.getFileName());
        ResultUtil.addVariableMetaData(FILE, varName, NO_EXPRESSION, valueResult, result);
        return () -> valueResult;
    }

    @Override
    public Supplier<String> getConstantResult(final FromConstant fromConstant,
                                    final String varName,
                                    final CommandResult result) {
        String valueResult = fromConstant.getValue();
        ResultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, valueResult, result);
        return () -> valueResult;
    }

    @Override
    public Supplier<String> getExpressionResult(final FromExpression fromExpression,
                                      final String varName,
                                      final CommandResult result) {
        String expression = fromExpression.getValue();
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);
        String valueResult = exp.getValue(String.class);
        ResultUtil.addVariableMetaData(EXPRESSION, varName, expression, valueResult, result);
        return () -> valueResult;
    }

    @Override
    @SneakyThrows
    public Supplier<String> getPathResult(final FromPath fromPath,
                                final String varName,
                                final ScenarioContext scenarioContext,
                                final CommandResult result) {
        String path = fromPath.getValue();
        String body = fromPath.getFrom() == null
                ? scenarioContext.getBody() : scenarioContext.get(fromPath.getFrom()).get();
        if (path.startsWith(DOLLAR_SIGN)) {
            return evaluateJPath(path, varName, body, result);
        }
        if (path.startsWith(SLASH_SEPARATOR)) {
            return evaluateXPath(path, varName, body, result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }


    private Supplier<String> evaluateXPath(final String path,
                                 final String varName,
                                 final String body,
                                 final CommandResult result) throws Exception {
        Document jsoupDoc = Jsoup.parse(body, "", Parser.xmlParser());
        org.w3c.dom.Document w3cDocument = convertJsoupToW3CDocument(jsoupDoc);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String valueResult = (String) xPath.evaluate(path, w3cDocument, XPathConstants.STRING);
        ResultUtil.addVariableMetaData(XML_PATH, varName, path, valueResult, result);
        return () -> valueResult;
    }

    private org.w3c.dom.Document convertJsoupToW3CDocument(final Document jsoupDoc) throws Exception {
        String htmlContent = jsoupDoc.html();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        InputSource inputSource = new InputSource(new StringReader(htmlContent));
        return factory.newDocumentBuilder().parse(inputSource);
    }

    private Supplier<String> evaluateJPath(final String path,
                                 final String varName,
                                 final String body,
                                 final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(body);
        String valueResult = Objects.nonNull(contextBody.read(path)) ? contextBody.read(path).toString() : null;
        ResultUtil.addVariableMetaData(JSON_PATH, varName, path, valueResult, result);
        return () -> valueResult;
    }

    @Override
    public Supplier<String> getSQLResult(final FromSQL fromSQL,
                               final String varName,
                               final CommandResult result) {
        String metadataKey = fromSQL.getDbType().name() + DelimiterConstant.UNDERSCORE + fromSQL.getAlias();
        AbstractStorageOperation storageOperation = aliasToStorageOperation.getByNameOrThrow(metadataKey);
        String valueResult = getActualQueryResult(fromSQL, storageOperation);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, fromSQL, varName, valueResult, result);
        return () -> valueResult;
    }

	@Override
	public Supplier<String> getGoogleAuthToken(GoogleAuthToken googleAuthToken, ApplicationContext context, ScenarioContext scenarioContext, String env, String varName, CommandResult result) {
		IntegrationsProvider integrationsProvider = context.getBean(IntegrationsProvider.class);
		List<GoogleAuth> list = integrationsProvider.findListByEnv(GoogleAuth.class, env);

		GoogleAuth googleAuth = integrationsProvider.findForAlias(list, googleAuthToken.getAlias());
		return () -> generateCode(googleAuth.getSecretKey());
	}

    private String getActualQueryResult(final FromSQL fromSQL, final AbstractStorageOperation storageOperation) {
        String alias = fromSQL.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(fromSQL.getQuery()));
        LogUtil.logAllQueries(fromSQL.getDbType().name(), singleQuery, alias);
        AbstractStorageOperation.StorageOperationResult queryResult = storageOperation.apply(
                new ListSource(singleQuery), alias);
        return getResultValue(queryResult, getKeyOfQueryResultValue(queryResult));
    }

    @SuppressWarnings("unchecked")
    private String getResultValue(final AbstractStorageOperation.StorageOperationResult storageOperationResult,
                                  final String key) {
        List<AbstractStorageOperation.QueryResult<?>> rawList =
                (List<AbstractStorageOperation.QueryResult<?>>) storageOperationResult.getRaw();
        List<LinkedCaseInsensitiveMap<String>> content =
                (List<LinkedCaseInsensitiveMap<String>>) rawList.get(0).getContent();
        verifyIfContentNotEmpty(content);
        Map<String, String> mapWithContent = content.get(0);
        return String.valueOf(mapWithContent.get(key));
    }

    private void verifyIfContentNotEmpty(final List<LinkedCaseInsensitiveMap<String>> content) {
        if (content.isEmpty()) {
            throw new DefaultFrameworkException(VAR_QUERY_RESULT_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private String getKeyOfQueryResultValue(final AbstractStorageOperation.StorageOperationResult applyRelationalDb) {
        List<AbstractStorageOperation.QueryResult<?>> rawList =
                (List<AbstractStorageOperation.QueryResult<?>>) applyRelationalDb.getRaw();
        String[] queryParts = rawList.get(0).getQuery().split(DelimiterConstant.SPACE);
        return queryParts[1];
    }

	private String generateCode(String secretKey) {
		try {
			long timeStep = 30L;
			long counter = Instant.now().getEpochSecond() / timeStep;

			Base32 base32 = new Base32();
			byte[] key = base32.decode(secretKey.toUpperCase().getBytes(StandardCharsets.UTF_8));

			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
			byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());

			int offset = hash[hash.length - 1] & 0xF;
			int binary =
					((hash[offset] & 0x7F) << 24) |
							((hash[offset + 1] & 0xFF) << 16) |
							((hash[offset + 2] & 0xFF) << 8) |
							(hash[offset + 3] & 0xFF);

			int otp = binary % 1_000_000;
			return String.format(SIX_DIGITS_FORMAT, otp);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
