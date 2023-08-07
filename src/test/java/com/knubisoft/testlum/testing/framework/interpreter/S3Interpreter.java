package com.knubisoft.testlum.testing.framework.interpreter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BUCKET_EXISTS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BUCKET_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_COMPARISON_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_PROCESSING_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_VALUE_COMPARISON_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_S3_PROCESSING;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FILE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALIAS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(S3.class)
public class S3Interpreter extends AbstractInterpreter<S3> {
    public static final String CREATE_BUCKET = "create bucket";
    public static final String REMOVE_BUCKET = "remove bucket";
    public static final String UPLOAD_FILE = "upload file";
    public static final String DOWNLOAD_FILE = "download file";
    public static final String REMOVE_FILE = "remove file";
    public static final String FILE_NAME = "File name";

    @Autowired(required = false)
    private Map<AliasEnv, AmazonS3> amazonS3;

    public S3Interpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final S3 o, final CommandResult result) {
        S3 s3 = injectCommand(o);
        LogUtil.logAlias(s3.getAlias());
        result.put(ALIAS, s3.getAlias());
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractCommand action : s3.getFileOrBucket()) {
            int commandId = dependencies.getPosition().incrementAndGet();
            LogUtil.logSubCommand(commandId, action);
            CommandResult commandResult = ResultUtil.newCommandResultInstance(commandId, action);
            subCommandsResult.add(commandResult);
            processEachAction(action, new AliasEnv(s3.getAlias(), dependencies.getEnvironment()), commandResult);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final AbstractCommand action, final AliasEnv aliasEnv, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processS3Action(aliasEnv, action, result);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
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
        String region = amazonS3.get(aliasEnv).getRegionName();
        if (isNotBlank(bucketCommand.getCreate())) {
            LogUtil.logS3BucketActionInfo(CREATE_BUCKET, bucketCommand.getCreate());
            ResultUtil.addS3BucketMetaData(CREATE_BUCKET, bucketCommand.getCreate(), result);
            createBucket(bucketCommand.getCreate(), region, aliasEnv);
        } else if (isNotBlank(bucketCommand.getRemove())) {
            LogUtil.logS3BucketActionInfo(REMOVE_BUCKET, bucketCommand.getRemove());
            ResultUtil.addS3BucketMetaData(REMOVE_BUCKET, bucketCommand.getRemove(), result);
            removeBucket(aliasEnv, bucketCommand.getRemove());
        }
    }

    private void createBucket(final String bucketName, final String region, final AliasEnv aliasEnv) {
        if (amazonS3.get(aliasEnv).doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_EXISTS, bucketName));
        }
        amazonS3.get(aliasEnv).createBucket(new CreateBucketRequest(bucketName, region));
    }

    private void removeBucket(final AliasEnv aliasEnv, final String bucketName) {
        checkBucketExist(aliasEnv, bucketName);
        ListObjectsV2Result listOfFiles = amazonS3.get(aliasEnv).listObjectsV2(bucketName);
        listOfFiles.getObjectSummaries()
                .forEach(object -> amazonS3.get(aliasEnv).deleteObject(bucketName, object.getKey()));
        amazonS3.get(aliasEnv).deleteBucket(bucketName);
    }

    private void checkBucketExist(final AliasEnv aliasEnv, final String bucketName) {
        if (!amazonS3.get(aliasEnv).doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
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
        LogUtil.logS3FileActionInfo(
                UPLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey());
        log.info(FILE_LOG, fileCommand.getUpload());
        ResultUtil.addS3FileMetaData(UPLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getUpload());
        result.put(FILE_NAME, fileCommand.getUpload());
        checkBucketExist(aliasEnv, bucketName);
        amazonS3.get(aliasEnv).putObject(bucketName, key, file);
    }

    private void processDownloadFileAction(final S3File fileCommand,
                                           final String bucketName,
                                           final String key,
                                           final AliasEnv aliasEnv,
                                           final CommandResult result) {
        LogUtil.logS3FileActionInfo(
                DOWNLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey());
        ResultUtil.addS3FileMetaData(DOWNLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        String expected = fileCommand.getDownload().getFile() == null
                ? fileCommand.getDownload().getValue()
                : FileSearcher.searchFileToString(fileCommand.getDownload().getFile(), dependencies.getFile());
        String actual = downloadFile(bucketName, key, aliasEnv);
        ResultUtil.setExpectedActual(expected, actual, result);
        compare(expected, actual, fileCommand.getDownload(), key, result);
        setContextBody(actual);
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
            S3Object s3Object = amazonS3.get(aliasEnv).getObject(bucketName, key);
            return IOUtils.toString(s3Object.getObjectContent(), StandardCharsets.UTF_8);
        } catch (AmazonS3Exception e) {
            throw new DefaultFrameworkException(String.format(FILE_PROCESSING_ERROR, key, bucketName));
        }
    }

    private void checkBucketFileExists(final String bucketName, final String key, final AliasEnv aliasEnv) {
        checkBucketExist(aliasEnv, bucketName);
        if (!amazonS3.get(aliasEnv).doesObjectExist(bucketName, key)) {
            throw new DefaultFrameworkException(String.format(FILE_NOT_FOUND, key, bucketName));
        }
    }

    private void processRemoveFileAction(final S3File fileCommand,
                                         final String bucketName,
                                         final String key,
                                         final AliasEnv aliasEnv,
                                         final CommandResult result) {
        LogUtil.logS3FileActionInfo(
                REMOVE_FILE, fileCommand.getBucket(), fileCommand.getKey());
        ResultUtil.addS3FileMetaData(REMOVE_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        checkBucketFileExists(bucketName, key, aliasEnv);
        amazonS3.get(aliasEnv).deleteObject(bucketName, key);
    }

}
