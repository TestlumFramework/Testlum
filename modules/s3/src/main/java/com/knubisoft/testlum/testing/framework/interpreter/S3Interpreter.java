package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.S3;
import com.knubisoft.testlum.testing.model.scenario.S3Bucket;
import com.knubisoft.testlum.testing.model.scenario.S3File;
import com.knubisoft.testlum.testing.model.scenario.S3FileDownload;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(S3.class)
public class S3Interpreter extends AbstractInterpreter<S3> {

    private static final String KEY = "Key";
    private static final String ALIAS = "Alias";
    private static final String ACTION = "Action";
    private static final String BUCKET = "Bucket";
    private static final String CREATE_BUCKET = "create bucket";
    private static final String REMOVE_BUCKET = "remove bucket";
    private static final String UPLOAD_FILE = "upload file";
    private static final String DOWNLOAD_FILE = "download file";
    private static final String REMOVE_FILE = "remove file";
    private static final String FILE_NAME = "File name";
    private static final String STEP_FAILED = "Step failed";
    private static final String INCORRECT_S3_PROCESSING = "Incorrect S3 processing";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    private static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    private static final String EXCEPTION_LOG = ANSI_RED
                                                + "----------------    EXCEPTION    -----------------"
                                                + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
                                                + "--------------------------------------------------" + ANSI_RESET;
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    public static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String S3_BUCKET_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Action", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "Bucket for action", "{}");
    private static final String S3_FILE_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Action", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "Bucket", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "Key", "{}");
    private static final String FILE_LOG = format(TABLE_FORMAT, "File", "{}");
    private static final String BUCKET_EXISTS = "Bucket with name <%s> already exists";
    private static final String BUCKET_NOT_FOUND = "Bucket with name <%s> is not found ";
    private static final String FILE_NOT_FOUND = "File with key <%s> in bucket <%s> is not found";
    private static final String FILE_PROCESSING_ERROR = "File with key <%s> in bucket <%s> can not be processed";
    private static final String FILE_COMPARISON_ERROR = "Actual file <%s> is not the same as expected file <%s>";
    private static final String FILE_VALUE_COMPARISON_ERROR = "Actual file <%s> is not the same as expected value <%s>";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, S3Client> s3Client;

    public S3Interpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final S3 o, final CommandResult result) {
        S3 s3 = injectCommand(o);
        checkAlias(s3);
        log.info(ALIAS_LOG, s3.getAlias());
        result.put(ALIAS, s3.getAlias());
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractCommand action : s3.getFileOrBucket()) {
            int commandId = dependencies.getPosition().incrementAndGet();
            log.info(COMMAND_LOG, commandId, action.getClass().getSimpleName());
            CommandResult commandResult = newCommandResultInstance(commandId, action);
            subCommandsResult.add(commandResult);
            processEachAction(action, new AliasEnv(s3.getAlias(), dependencies.getEnvironment()), commandResult);
        }
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void checkAlias(final S3 s3) {
        if (s3.getAlias() == null) {
            s3.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void processEachAction(final AbstractCommand action, final AliasEnv aliasEnv, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processS3Action(aliasEnv, action, result);
        } catch (Exception e) {
            logException(e);
            setExceptionResult(result, e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    @SneakyThrows
    private void processS3Action(final AliasEnv aliasEnv, final AbstractCommand s3, final CommandResult result) {
        if (s3 instanceof S3Bucket) {
            processBucketAction((S3Bucket) s3, aliasEnv, result);
        } else if (s3 instanceof S3File) {
            processFileAction((S3File) s3, aliasEnv, result);
        } else {
            throw new DefaultFrameworkException(INCORRECT_S3_PROCESSING);
        }
    }

    private void processBucketAction(final S3Bucket bucketCommand,
                                     final AliasEnv aliasEnv,
                                     final CommandResult result) {
        if (isNotBlank(bucketCommand.getCreate())) {
            logS3BucketActionInfo(CREATE_BUCKET, bucketCommand.getCreate());
            addS3BucketMetaData(CREATE_BUCKET, bucketCommand.getCreate(), result);
            createBucket(bucketCommand.getCreate(), aliasEnv);
        } else if (isNotBlank(bucketCommand.getRemove())) {
            logS3BucketActionInfo(REMOVE_BUCKET, bucketCommand.getRemove());
            addS3BucketMetaData(REMOVE_BUCKET, bucketCommand.getRemove(), result);
            removeBucket(aliasEnv, bucketCommand.getRemove());
        }
    }

    private void createBucket(final String bucketName, final AliasEnv aliasEnv) {
        if (doesBucketExist(aliasEnv, bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_EXISTS, bucketName));
        }
        s3Client.get(aliasEnv).createBucket(request -> request.bucket(bucketName));
    }

    private void removeBucket(final AliasEnv aliasEnv, final String bucketName) {
        checkBucketExist(aliasEnv, bucketName);
        ListObjectsV2Response listOfFiles = s3Client.get(aliasEnv).listObjectsV2(builder -> builder.bucket(bucketName));
        listOfFiles.contents().forEach(object -> s3Client.get(aliasEnv)
                .deleteObject(builder -> builder.bucket(bucketName).key(object.key())));
        s3Client.get(aliasEnv).deleteBucket(request -> request.bucket(bucketName));
    }

    private void checkBucketExist(final AliasEnv aliasEnv, final String bucketName) {
        if (!doesBucketExist(aliasEnv, bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
        }
    }

    private boolean doesBucketExist(final AliasEnv aliasEnv, final String bucketName) {
        try {
            s3Client.get(aliasEnv).headBucket(request -> request.bucket(bucketName));
            return true;
        } catch (final NoSuchBucketException exception) {
            return false;
        }
    }

    private void processFileAction(final S3File fileCommand,
                                   final AliasEnv aliasEnv,
                                   final CommandResult result) {
        String key = fileCommand.getKey();
        String bucketName = fileCommand.getBucket();
        if (isNotBlank(fileCommand.getUpload())) {
            processUploadFileAction(fileCommand, bucketName, key, aliasEnv, result);
        }
        if (nonNull(fileCommand.getDownload())) {
            processDownloadFileAction(fileCommand, bucketName, key, aliasEnv, result);
        }
        if (nonNull(fileCommand.getRemove())) {
            processRemoveFileAction(fileCommand, bucketName, key, aliasEnv, result);
        }
    }

    private void processUploadFileAction(final S3File fileCommand,
                                         final String bucketName,
                                         final String key,
                                         final AliasEnv aliasEnv,
                                         final CommandResult result) {
        logS3FileActionInfo(
                UPLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey());
        log.info(FILE_LOG, fileCommand.getUpload());
        addS3FileMetaData(UPLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getUpload());
        result.put(FILE_NAME, fileCommand.getUpload());
        checkBucketExist(aliasEnv, bucketName);
        s3Client.get(aliasEnv).putObject(builder -> builder.bucket(bucketName).key(key), file.toPath());
    }

    private void processDownloadFileAction(final S3File fileCommand,
                                           final String bucketName,
                                           final String key,
                                           final AliasEnv aliasEnv,
                                           final CommandResult result) {
        logS3FileActionInfo(
                DOWNLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey());
        addS3FileMetaData(DOWNLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        String expected = fileCommand.getDownload().getFile() == null
                ? fileCommand.getDownload().getValue()
                : FileSearcher.searchFileToString(fileCommand.getDownload().getFile(), dependencies.getFile());
        String actual = downloadFile(bucketName, key, aliasEnv);
        setExpectedActual(expected, actual, result);
        compare(expected, actual, fileCommand.getDownload(), key, result);
        setContextBody(getContextBodyKey(getFileOrDownloadValue(fileCommand)), actual);
    }

    private void compare(final String expected,
                         final String actual,
                         final S3FileDownload s3FileDownload,
                         final String key,
                         final CommandResult result) {
        try {
            CompareBuilder comparator = newCompare().withExpected(expected).withActual(actual);
            comparator.exec();
        } catch (ComparisonException e) {
            ComparisonException comparisonException = s3FileDownload.getFile() == null
                    ? new ComparisonException(String.format(
                    FILE_VALUE_COMPARISON_ERROR, key, s3FileDownload.getValue()))
                    : new ComparisonException(String.format(
                    FILE_COMPARISON_ERROR, key, s3FileDownload.getFile()));
            result.setException(comparisonException);
            throw comparisonException;
        }
    }

    @SneakyThrows
    private String downloadFile(final String bucketName,
                                final String key,
                                final AliasEnv aliasEnv) {
        try {
            checkBucketFileExists(bucketName, key, aliasEnv);
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.get(aliasEnv)
                    .getObject(builder -> builder.bucket(bucketName).key(key));
            return IOUtils.toString(s3Object, StandardCharsets.UTF_8);
        } catch (S3Exception e) {
            throw new DefaultFrameworkException(String.format(FILE_PROCESSING_ERROR, key, bucketName));
        }
    }

    private void checkBucketFileExists(final String bucketName, final String key, final AliasEnv aliasEnv) {
        checkBucketExist(aliasEnv, bucketName);
        if (!doesObjectExist(aliasEnv, bucketName, key)) {
            throw new DefaultFrameworkException(String.format(FILE_NOT_FOUND, key, bucketName));
        }
    }

    private boolean doesObjectExist(final AliasEnv aliasEnv, final String bucketName, final String key) {
        try {
            s3Client.get(aliasEnv).headObject(builder -> builder.bucket(bucketName).key(key));
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    private void processRemoveFileAction(final S3File fileCommand,
                                         final String bucketName,
                                         final String key,
                                         final AliasEnv aliasEnv,
                                         final CommandResult result) {
        logS3FileActionInfo(
                REMOVE_FILE, fileCommand.getBucket(), fileCommand.getKey());
        addS3FileMetaData(REMOVE_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        checkBucketFileExists(bucketName, key, aliasEnv);
        s3Client.get(aliasEnv).deleteObject(builder ->
                builder.bucket(bucketName)
                        .key(key)
                        .build()
        );
    }

    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    private void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    private void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    private void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    private void logS3BucketActionInfo(final String action, final String bucket) {
        log.info(S3_BUCKET_ACTION_INFO_LOG, action.toUpperCase(Locale.ROOT), bucket);
    }

    private void logS3FileActionInfo(final String action, final String bucket, final String key) {
        log.info(S3_FILE_ACTION_INFO_LOG, action.toUpperCase(Locale.ROOT), bucket, key);
    }

    private void addS3BucketMetaData(final String action, final String bucket, final CommandResult result) {
        result.put(ACTION, action);
        result.put(BUCKET, bucket);
    }

    private void addS3FileMetaData(final String action,
                                   final String bucket,
                                   final String key,
                                   final CommandResult result) {
        addS3BucketMetaData(action, bucket, result);
        result.put(KEY, key);
    }

    private void setExpectedActual(final String expected, final String actual, final CommandResult result) {
        result.setExpected(expected);
        result.setActual(actual);
    }

    private String getFileOrDownloadValue(final S3File fileCommand) {
        S3FileDownload download = fileCommand.getDownload();
        return download.getFile() == null ? download.getValue() : download.getFile();
    }

}