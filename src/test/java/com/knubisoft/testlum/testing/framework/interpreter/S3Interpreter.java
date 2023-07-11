package com.knubisoft.testlum.testing.framework.interpreter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.S3;
import com.knubisoft.testlum.testing.model.scenario.S3Bucket;
import com.knubisoft.testlum.testing.model.scenario.S3File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BUCKET_EXISTS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BUCKET_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_PROCESSING_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_S3_PROCESSING;
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
            processEachAction(new AliasEnv(s3.getAlias(), dependencies.getEnvironment()), action, commandResult);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final AliasEnv aliasEnv, final AbstractCommand action, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processS3Action(aliasEnv, action, result);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    @SneakyThrows
    private void processS3Action(final AliasEnv aliasEnv, final AbstractCommand s3, final CommandResult result) {
        if (s3 instanceof S3Bucket) {
            processBucketAction(aliasEnv, (S3Bucket) s3, result);
        } else if (s3 instanceof S3File) {
            processFileAction(aliasEnv, (S3File) s3, result);
        } else {
            throw new DefaultFrameworkException(INCORRECT_S3_PROCESSING);
        }
    }

    private void processBucketAction(final AliasEnv aliasEnv,
                                     final S3Bucket bucketCommand,
                                     final CommandResult result) {
        String region = amazonS3.get(aliasEnv).getRegionName();
        if (isNotBlank(bucketCommand.getCreate())) {
            LogUtil.logS3BucketActionInfo(CREATE_BUCKET, bucketCommand.getCreate());
            ResultUtil.addS3BucketMetaData(CREATE_BUCKET, bucketCommand.getCreate(), result);
            createBucket(aliasEnv, bucketCommand.getCreate(), region);
        } else if (isNotBlank(bucketCommand.getRemove())) {
            LogUtil.logS3BucketActionInfo(REMOVE_BUCKET, bucketCommand.getRemove());
            ResultUtil.addS3BucketMetaData(REMOVE_BUCKET, bucketCommand.getRemove(), result);
            removeBucket(aliasEnv, bucketCommand.getRemove());
        }
    }

    private void createBucket(final AliasEnv aliasEnv, final String bucketName, final String region) {
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

    private void processFileAction(final AliasEnv aliasEnv,
                                   final S3File fileCommand, final
                                   CommandResult result) throws IOException {
        String key = fileCommand.getKey();
        String bucketName = fileCommand.getBucket();
        if (isNotBlank(fileCommand.getUpload())) {
            executeUploadFile(aliasEnv, fileCommand, bucketName, key, result);
        } else if (nonNull(fileCommand.getDownload())) {
            executeDownloadFile(aliasEnv, fileCommand, bucketName, key, result);
        } else if (isNotBlank(fileCommand.getRemove())) {
            removeFile(aliasEnv, fileCommand, bucketName, key, result);
        }
    }

    private void executeUploadFile(final AliasEnv aliasEnv,
                                   final S3File fileCommand,
                                   final String bucketName,
                                   final String key,
                                   final CommandResult result) {
        LogUtil.logS3FileActionInfo(
                UPLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), fileCommand.getUpload());
        ResultUtil.addS3FileMetaData(UPLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        uploadFile(aliasEnv, fileCommand, bucketName, key, result);
    }

    private void uploadFile(final AliasEnv aliasEnv,
                            final S3File fileCommand,
                            final String bucketName,
                            final String key,
                            final CommandResult result) {
        final File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getUpload());
        result.put(FILE_NAME, fileCommand.getUpload());
        checkBucketExist(aliasEnv, bucketName);
        amazonS3.get(aliasEnv).putObject(bucketName, key, file);
    }

    private void executeDownloadFile(final AliasEnv aliasEnv,
                                     final S3File fileCommand,
                                     final String bucketName,
                                     final String key,
                                     final CommandResult result) throws IOException {
        LogUtil.logS3FileActionInfo(
                DOWNLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), fileCommand.getDownload());
        ResultUtil.addS3FileMetaData(DOWNLOAD_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        setContextBody(downloadFile(aliasEnv, fileCommand.getDownload(), bucketName, key, result));
    }

    private String downloadFile(final AliasEnv aliasEnv,
                                final String fileName,
                                final String bucketName,
                                final String key,
                                final CommandResult result) throws IOException {
        File expectedFile = FileSearcher.searchFileFromDir(dependencies.getFile(), fileName);
        InputStream expectedStream = FileUtils.openInputStream(expectedFile);
        String expected = IOUtils.toString(expectedStream, StandardCharsets.UTF_8);
        String actual = downloadFile(aliasEnv, bucketName, key).orElse(null);
        result.setExpected(expected);
        result.setActual(actual);

        CompareBuilder comparator = newCompare()
                .withExpected(expected)
                .withActual(actual);
        comparator.exec();
        return actual;
    }

    private Optional<String> downloadFile(final AliasEnv aliasEnv,
                                          final String bucketName,
                                          final String key) throws IOException {
        try {
            checkBucketFileExists(aliasEnv, bucketName, key);
            S3Object s3Object = amazonS3.get(aliasEnv).getObject(bucketName, key);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            return Optional.of(IOUtils.toString(s3ObjectInputStream, StandardCharsets.UTF_8));
        } catch (AmazonS3Exception e) {
            throw new DefaultFrameworkException(String.format(FILE_PROCESSING_ERROR, key, bucketName));
        }
    }

    private void checkBucketFileExists(final AliasEnv aliasEnv, final String bucketName, final String key) {
        checkBucketExist(aliasEnv, bucketName);
        if (!amazonS3.get(aliasEnv).doesObjectExist(bucketName, key)) {
            throw new DefaultFrameworkException(String.format(FILE_NOT_FOUND, key, bucketName));
        }
    }

    private void checkBucketExist(final AliasEnv aliasEnv, final String bucketName) {
        if (!amazonS3.get(aliasEnv).doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
        }
    }

    private void removeFile(final AliasEnv aliasEnv,
                            final S3File fileCommand,
                            final String bucketName,
                            final String key,
                            final CommandResult result) {
        LogUtil.logS3FileActionInfo(
                REMOVE_FILE, fileCommand.getBucket(), fileCommand.getKey(), fileCommand.getRemove());
        ResultUtil.addS3FileMetaData(REMOVE_FILE, fileCommand.getBucket(), fileCommand.getKey(), result);
        checkBucketFileExists(aliasEnv, bucketName, key);
        amazonS3.get(aliasEnv).deleteObject(bucketName, fileCommand.getRemove());
    }

}
