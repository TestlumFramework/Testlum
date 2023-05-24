package com.knubisoft.testlum.testing.framework.interpreter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.S3;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_S3_PROCESSING;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ALIAS_LOG;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(S3.class)
public class S3Interpreter extends AbstractInterpreter<S3> {

    private static final String UPLOAD_ACTION = "upload";
    private static final String DOWNLOAD_ACTION = "download";

    @Autowired(required = false)
    private Map<AliasEnv, AmazonS3> amazonS3;

    public S3Interpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final S3 s3, final CommandResult result) {
        log.info(ALIAS_LOG, s3.getAlias());
        String bucket = s3.getAlias();
        String key = inject(s3.getKey());
        exec(s3, bucket, key, result);
    }

    //CHECKSTYLE:OFF
    @SneakyThrows
    private void exec(final S3 s3, final String bucket, final String key, final CommandResult result) {
        if (isNotBlank(s3.getUpload())) {
            ResultUtil.addS3GeneralMetaData(bucket, UPLOAD_ACTION, key, bucket, result);
            final String fileName = inject(s3.getUpload());
            final File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileName);
            result.put("File name", fileName);
            LogUtil.logS3ActionInfo(UPLOAD_ACTION, bucket, key, fileName);
            AliasEnv aliasEnv = new AliasEnv(bucket, dependencies.getEnvironment());
            this.amazonS3.get(aliasEnv).createBucket(bucket);
            this.amazonS3.get(aliasEnv).putObject(bucket, key, file);
        } else if (isNotBlank(s3.getDownload())) {
            ResultUtil.addS3GeneralMetaData(bucket, DOWNLOAD_ACTION, key, bucket, result);
            setContextBody(downloadAndCompareFile(bucket, key, inject(s3.getDownload()), result));
        }
        throw new DefaultFrameworkException(INCORRECT_S3_PROCESSING);
    }

    private String downloadAndCompareFile(final String bucket,
                                          final String key,
                                          final String fileName,
                                          final CommandResult result) throws IOException {
        LogUtil.logS3ActionInfo(DOWNLOAD_ACTION, bucket, key, fileName);
        File expectedFile = FileSearcher.searchFileFromDir(dependencies.getFile(), fileName);
        InputStream expectedStream = FileUtils.openInputStream(expectedFile);
        String expected = IOUtils.toString(expectedStream, StandardCharsets.UTF_8);
        String actual = downloadFile(bucket, key).orElse(null);
        CompareBuilder comparator = newCompare()
                .withExpected(expected)
                .withActual(actual);
        result.setExpected(expected);
        result.setActual(actual);
        comparator.exec();
        return actual;
    }
    //CHECKSTYLE:ON

    private Optional<String> downloadFile(final String bucket, final String key) throws IOException {
        try {
            AliasEnv aliasEnv = new AliasEnv(bucket, dependencies.getEnvironment());
            S3Object s3Object = amazonS3.get(aliasEnv).getObject(bucket, key);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            String actual = IOUtils.toString(s3ObjectInputStream, StandardCharsets.UTF_8);
            return Optional.of(actual);
        } catch (AmazonS3Exception e) {
            return Optional.empty();
        }
    }
}
