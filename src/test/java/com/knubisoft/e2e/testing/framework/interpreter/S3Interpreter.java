package com.knubisoft.e2e.testing.framework.interpreter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.S3;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.LogMessage;
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

@Slf4j
@InterpreterForClass(S3.class)
public class S3Interpreter extends AbstractInterpreter<S3> {

    @Autowired(required = false)
    private Map<String, AmazonS3> amazonS3;

    public S3Interpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final S3 s3, final CommandResult result) {
        String bucket = inject(s3.getAlias());
        String key = inject(s3.getKey());

        result.put("key", key);
        result.put("bucket", bucket);
        exec(s3, bucket, key, result);
    }

    //CHECKSTYLE:OFF
    private void exec(final S3 s3, final String bucket, final String key, final CommandResult result) {
        if (s3.getUpload() != null) {
            result.put("action", "upload");
            final String fileName = inject(s3.getUpload());
            final File file = this.dependencies.getFileSearcher().search(fileName);
            result.put("fileName", fileName);
            this.amazonS3.get(bucket).createBucket(bucket);
            this.amazonS3.get(bucket).putObject(bucket, key, file);
        } else if (s3.getDownload() != null) {
            result.put("action", "download");
            setContextBody(downloadAndCompareFile(bucket, key, inject(s3.getDownload()), result));
        } else {
            throw new DefaultFrameworkException(LogMessage.INCORRECT_S3_PROCESSING);
        }
    }

    @SneakyThrows
    private String downloadAndCompareFile(final String bucket,
                                          final String key,
                                          final String fileName,
                                          final CommandResult result) {
        File expectedFile = this.dependencies.getFileSearcher().search(fileName);
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
            S3Object s3Object = amazonS3.get(bucket).getObject(bucket, key);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            String actual = IOUtils.toString(s3ObjectInputStream, StandardCharsets.UTF_8);
            return Optional.of(actual);
        } catch (AmazonS3Exception e) {
            return Optional.empty();
        }
    }


}
