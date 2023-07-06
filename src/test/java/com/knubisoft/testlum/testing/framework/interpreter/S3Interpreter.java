package com.knubisoft.testlum.testing.framework.interpreter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BUCKET_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BUCKET_EXISTS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_PROCESSING_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_S3_PROCESSING;
import static com.knubisoft.testlum.testing.framework.util.S3Util.CREATE_BUCKET;
import static com.knubisoft.testlum.testing.framework.util.S3Util.DOWNLOAD_FILE;
import static com.knubisoft.testlum.testing.framework.util.S3Util.REMOVE_BUCKET;
import static com.knubisoft.testlum.testing.framework.util.S3Util.REMOVE_FILE;
import static com.knubisoft.testlum.testing.framework.util.S3Util.UPLOAD_FILE;
import static com.knubisoft.testlum.testing.framework.util.S3Util.logAndReportAlias;
import static com.knubisoft.testlum.testing.framework.util.S3Util.logAndReportBucketInfo;
import static com.knubisoft.testlum.testing.framework.util.S3Util.logAndReportFileInfo;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(S3.class)
public class S3Interpreter extends AbstractInterpreter<S3> {

    public static final String FILE_NAME = "File name";
    @Autowired(required = false)
    private Map<AliasEnv, AmazonS3> amazonS3;

    public S3Interpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final S3 o, final CommandResult result) {
        S3 s3 = injectCommand(o);
        logAndReportAlias(s3, result);
        AliasEnv aliasEnv = new AliasEnv(s3.getAlias(), dependencies.getEnvironment());
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractCommand action : s3.getFileOrBucket()) {
            int commandId = dependencies.getPosition().incrementAndGet();
            LogUtil.logSubCommand(commandId, action);
            CommandResult commandResult = ResultUtil.newCommandResultInstance(commandId, action);
            subCommandsResult.add(commandResult);
            processEachAction(action, aliasEnv, commandResult);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final AbstractCommand action, final AliasEnv aliasEnv, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processS3Action(action, aliasEnv, result);
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
    private void processS3Action(final AbstractCommand s3, final AliasEnv aliasEnv, final CommandResult result) {
        if (s3 instanceof S3Bucket) {
            processBucketAction((S3Bucket) s3, aliasEnv, result);
        } else if (s3 instanceof S3File) {
            processFileAction((S3File) s3, aliasEnv, result);
        } else {
            throw new DefaultFrameworkException(INCORRECT_S3_PROCESSING);
        }
    }

    private void processBucketAction(final S3Bucket bucket, final AliasEnv aliasEnv, final CommandResult result) {
        String region = amazonS3.get(aliasEnv).getRegionName();
        if (isNotBlank(bucket.getCreate())) {
            executeCreateBucket(aliasEnv, result, bucket, region);
        } else if (isNotBlank(bucket.getRemove())) {
            executeRemoveBucket(aliasEnv, result, bucket);
        }
    }

    private void processFileAction(final S3File file,
                                   final AliasEnv aliasEnv,
                                   final CommandResult result) throws IOException {
        String key = file.getKey();
        String bucketName = file.getBucket();
        if (isNotBlank(file.getUpload())) {
            executeUploadFile(aliasEnv, result, file, key, bucketName);
        } else if (nonNull(file.getDownload())) {
            executeDownloadFile(aliasEnv, result, file, key, bucketName);
        } else if (isNotBlank(file.getRemove())) {
            executeRemoveFile(aliasEnv, result, file, key, bucketName);
        }
    }

    private void executeCreateBucket(final AliasEnv aliasEnv,
                                     final CommandResult result,
                                     final S3Bucket bucketCommand,
                                     final String region) {
        logAndReportBucketInfo(CREATE_BUCKET, bucketCommand.getCreate(), result);
        createBucket(bucketCommand.getExpected(), aliasEnv, bucketCommand.getCreate(), region, result);
    }

    private void executeRemoveBucket(final AliasEnv aliasEnv,
                                     final CommandResult result,
                                     final S3Bucket bucketCommand) {
        logAndReportBucketInfo(REMOVE_BUCKET, bucketCommand.getRemove(), result);
        removeBucket(bucketCommand.getExpected(), aliasEnv, bucketCommand.getRemove(), result);
    }

    private void createBucket(final String fileName,
                              final AliasEnv aliasEnv,
                              final String bucketName,
                              final String region,
                              final CommandResult result) {
        if (amazonS3.get(aliasEnv).doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_EXISTS, bucketName));
        }
        amazonS3.get(aliasEnv).createBucket(new CreateBucketRequest(bucketName, region));
        compareBucketActionResult(fileName, aliasEnv, result);
    }

    private void removeBucket(final String expectedFileName,
                              final AliasEnv aliasEnv,
                              final String bucketName,
                              final CommandResult result) {
        AmazonS3 s3Client = amazonS3.get(aliasEnv);
        if (!s3Client.doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
        }
        ListObjectsV2Result listOfFiles = s3Client.listObjectsV2(bucketName);
        listOfFiles.getObjectSummaries().forEach(object -> s3Client.deleteObject(bucketName, object.getKey()));
        s3Client.deleteBucket(bucketName);
        compareBucketActionResult(expectedFileName, aliasEnv, result);
    }

    private void compareBucketActionResult(final String expectedFileName,
                                           final AliasEnv aliasEnv,
                                           final CommandResult result) {
        String actual = getBuckets(amazonS3.get(aliasEnv)).toString();
        String expected = getContentIfFile(expectedFileName);
        compare(result, expected, actual);
    }

    private List<String> getBuckets(final AmazonS3 client) {
        return client.listBuckets().stream()
                .map(Bucket::getName)
                .collect(Collectors.toList());
    }

    private void compare(final CommandResult result, final String expected, final String actual) {
        ResultUtil.setExpectedActual(expected, actual, result);
        CompareBuilder comparator = newCompare()
                .withExpected(expected)
                .withActual(actual);
        comparator.exec();
    }

    private void executeUploadFile(final AliasEnv aliasEnv,
                                   final CommandResult result,
                                   final S3File fileCommand,
                                   final String key,
                                   final String bucketName) {
        logAndReportFileInfo(UPLOAD_FILE, fileCommand, fileCommand.getUpload(), result);
        uploadFile(fileCommand, bucketName, key, aliasEnv, result);
    }

    private void uploadFile(final S3File fileCommand,
                            final String bucketName,
                            final String key,
                            final AliasEnv aliasEnv,
                            final CommandResult result) {
        final File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getUpload());
        result.put(FILE_NAME, fileCommand.getUpload());
        AmazonS3 s3Client = amazonS3.get(aliasEnv);
        if (!s3Client.doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
        }
        s3Client.putObject(bucketName, key, file);
        compareFileActionResult(fileCommand.getExpected(), bucketName, aliasEnv, result);
    }

    private void executeDownloadFile(final AliasEnv aliasEnv,
                                     final CommandResult result,
                                     final S3File fileCommand,
                                     final String key,
                                     final String bucketName) throws IOException {
        logAndReportFileInfo(DOWNLOAD_FILE, fileCommand, fileCommand.getDownload().toString(), result);
        setContextBody(downloadFile(fileCommand, bucketName, key, aliasEnv, result));
    }

    private void executeRemoveFile(final AliasEnv aliasEnv,
                                   final CommandResult result,
                                   final S3File fileCommand,
                                   final String key,
                                   final String bucketName) {
        logAndReportFileInfo(REMOVE_FILE, fileCommand, fileCommand.getRemove(), result);
        removeFile(fileCommand, bucketName, key, aliasEnv, result);
    }


    private void compareFileActionResult(final String expectedFileName,
                                         final String bucketName,
                                         final AliasEnv aliasEnv,
                                         final CommandResult result) {
        String actual = getFilesInBucket(amazonS3.get(aliasEnv), bucketName).toString();
        String expected = getContentIfFile(expectedFileName);
        compare(result, expected, actual);
    }


    private List<String> getFilesInBucket(final AmazonS3 client, final String bucketName) {
        return client.listObjectsV2(bucketName)
                .getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    private String downloadFile(final S3File fileCommand,
                                final String bucketName,
                                final String key,
                                final AliasEnv aliasEnv,
                                final CommandResult result) throws IOException {
        String actual = downloadFile(bucketName, key, aliasEnv).orElse(null);
        File expectedFile = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getExpected());
        InputStream expectedStream = FileUtils.openInputStream(expectedFile);
        String expected = IOUtils.toString(expectedStream, StandardCharsets.UTF_8);
        compare(result, expected, actual);
        return actual;
    }

    private Optional<String> downloadFile(final String bucketName,
                                          final String key,
                                          final AliasEnv aliasEnv) throws IOException {
        try {
            checkFileCommand(bucketName, key, aliasEnv);
            S3Object s3Object = amazonS3.get(aliasEnv).getObject(bucketName, key);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            return Optional.of(IOUtils.toString(s3ObjectInputStream, StandardCharsets.UTF_8));
        } catch (AmazonS3Exception e) {
            throw new DefaultFrameworkException(String.format(FILE_PROCESSING_ERROR, key, bucketName));
        }
    }

    private void checkFileCommand(final String bucketName, final String key, final AliasEnv aliasEnv) {
        if (!amazonS3.get(aliasEnv).doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
        }
        if (!amazonS3.get(aliasEnv).doesObjectExist(bucketName, key)) {
            throw new DefaultFrameworkException(String.format(FILE_NOT_FOUND, key, bucketName));
        }
    }

    private void removeFile(final S3File fileCommand,
                            final String bucketName,
                            final String key,
                            final AliasEnv aliasEnv,
                            final CommandResult result) {
        checkFileCommand(bucketName, key, aliasEnv);
        amazonS3.get(aliasEnv).deleteObject(bucketName, fileCommand.getRemove());
        compareFileActionResult(fileCommand.getExpected(), bucketName, aliasEnv, result);
    }
}
