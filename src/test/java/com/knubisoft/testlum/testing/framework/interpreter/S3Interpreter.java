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
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALIAS;
import static com.knubisoft.testlum.testing.framework.util.S3Util.CREATE_BUCKET;
import static com.knubisoft.testlum.testing.framework.util.S3Util.DOWNLOAD_FILE;
import static com.knubisoft.testlum.testing.framework.util.S3Util.FILE_NAME;
import static com.knubisoft.testlum.testing.framework.util.S3Util.REMOVE_BUCKET;
import static com.knubisoft.testlum.testing.framework.util.S3Util.REMOVE_FILE;
import static com.knubisoft.testlum.testing.framework.util.S3Util.UPLOAD_FILE;
import static com.knubisoft.testlum.testing.framework.util.S3Util.logAndReportBucketInfo;
import static com.knubisoft.testlum.testing.framework.util.S3Util.logAndReportFileInfo;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(S3.class)
public class S3Interpreter extends AbstractInterpreter<S3> {
    @Autowired(required = false)
    private Map<AliasEnv, AmazonS3> amazonS3Map;
    private AmazonS3 amazonS3;

    public S3Interpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final S3 o, final CommandResult result) {
        S3 s3 = injectCommand(o);
        amazonS3 = amazonS3Map.get(new AliasEnv(s3.getAlias(), dependencies.getEnvironment()));
        logAndReport(result, s3);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractCommand action : s3.getFileOrBucket()) {
            int commandId = dependencies.getPosition().incrementAndGet();
            LogUtil.logSubCommand(commandId, action);
            CommandResult commandResult = ResultUtil.newCommandResultInstance(commandId, action);
            subCommandsResult.add(commandResult);
            processEachAction(action, commandResult);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private static void logAndReport(final CommandResult result, final S3 s3) {
        LogUtil.logAlias(s3.getAlias());
        result.put(ALIAS, s3.getAlias());
    }

    private void processEachAction(final AbstractCommand action, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processS3Action(action, result);
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
    private void processS3Action(final AbstractCommand s3, final CommandResult result) {
        if (s3 instanceof S3Bucket) {
            processBucketAction((S3Bucket) s3, result);
        } else if (s3 instanceof S3File) {
            processFileAction((S3File) s3, result);
        } else {
            throw new DefaultFrameworkException(INCORRECT_S3_PROCESSING);
        }
    }

    private void processBucketAction(final S3Bucket bucket, final CommandResult result) {
        String region = amazonS3.getRegionName();
        if (isNotBlank(bucket.getCreate())) {
            executeCreateBucket(result, bucket, region);
        } else if (isNotBlank(bucket.getRemove())) {
            executeRemoveBucket(result, bucket);
        }
    }

    private void executeCreateBucket(final CommandResult result, final S3Bucket bucketCommand, final String region) {
        logAndReportBucketInfo(CREATE_BUCKET, bucketCommand.getCreate(), result);
        createBucket(bucketCommand.getExpected(), bucketCommand.getCreate(), region, result);
    }

    private void createBucket(final String fileName,
                              final String bucketName,
                              final String region,
                              final CommandResult result) {
        if (amazonS3.doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_EXISTS, bucketName));
        }
        amazonS3.createBucket(new CreateBucketRequest(bucketName, region));
        compareBucketActionResult(fileName, result);
    }

    private void compareBucketActionResult(final String expectedFileName, final CommandResult result) {
        String actual = getBuckets(amazonS3).toString();
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

    private void executeRemoveBucket(final CommandResult result, final S3Bucket bucketCommand) {
        logAndReportBucketInfo(REMOVE_BUCKET, bucketCommand.getRemove(), result);
        removeBucket(bucketCommand.getExpected(), bucketCommand.getRemove(), result);
    }

    private void removeBucket(final String expectedFileName, final String bucketName, final CommandResult result) {
        checkBucketExist(bucketName);
        ListObjectsV2Result listOfFiles = amazonS3.listObjectsV2(bucketName);
        listOfFiles.getObjectSummaries().forEach(object -> amazonS3.deleteObject(bucketName, object.getKey()));
        amazonS3.deleteBucket(bucketName);
        compareBucketActionResult(expectedFileName, result);
    }

    private void checkBucketExist(final String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            throw new DefaultFrameworkException(String.format(BUCKET_NOT_FOUND, bucketName));
        }
    }

    private void processFileAction(final S3File file, final CommandResult result) throws IOException {
        String key = file.getKey();
        String bucketName = file.getBucket();
        if (isNotBlank(file.getUpload())) {
            executeUploadFile(result, file, key, bucketName);
        } else if (nonNull(file.getDownload())) {
            executeDownloadFile(result, file, key, bucketName);
        } else if (isNotBlank(file.getRemove())) {
            executeRemoveFile(result, file, key, bucketName);
        }
    }

    private void executeUploadFile(final CommandResult result,
                                   final S3File fileCommand,
                                   final String key,
                                   final String bucketName) {
        logAndReportFileInfo(UPLOAD_FILE, fileCommand, fileCommand.getUpload(), result);
        uploadFile(fileCommand, bucketName, key, result);
    }

    private void uploadFile(final S3File fileCommand,
                            final String bucketName,
                            final String key,
                            final CommandResult result) {
        final File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getUpload());
        result.put(FILE_NAME, fileCommand.getUpload());
        checkBucketExist(bucketName);
        amazonS3.putObject(bucketName, key, file);
        compareFileActionResult(fileCommand.getExpected(), bucketName, result);
    }

    private void executeDownloadFile(final CommandResult result,
                                     final S3File fileCommand,
                                     final String key,
                                     final String bucketName) throws IOException {
        logAndReportFileInfo(DOWNLOAD_FILE, fileCommand, fileCommand.getDownload().toString(), result);
        setContextBody(downloadFile(fileCommand, bucketName, key, result));
    }

    private void executeRemoveFile(final CommandResult result,
                                   final S3File fileCommand,
                                   final String key,
                                   final String bucketName) {
        logAndReportFileInfo(REMOVE_FILE, fileCommand, fileCommand.getRemove(), result);
        removeFile(fileCommand, bucketName, key, result);
    }


    private void compareFileActionResult(final String expectedFileName,
                                         final String bucketName,
                                         final CommandResult result) {
        String actual = getFilesInBucket(amazonS3, bucketName).toString();
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
                                final CommandResult result) throws IOException {
        String actual = downloadFile(bucketName, key).orElse(null);
        File expectedFile = FileSearcher.searchFileFromDir(dependencies.getFile(), fileCommand.getExpected());
        InputStream expectedStream = FileUtils.openInputStream(expectedFile);
        String expected = IOUtils.toString(expectedStream, StandardCharsets.UTF_8);
        compare(result, expected, actual);
        return actual;
    }

    private Optional<String> downloadFile(final String bucketName, final String key) throws IOException {
        try {
            checkFileCommand(bucketName, key);
            S3Object s3Object = amazonS3.getObject(bucketName, key);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            return Optional.of(IOUtils.toString(s3ObjectInputStream, StandardCharsets.UTF_8));
        } catch (AmazonS3Exception e) {
            throw new DefaultFrameworkException(String.format(FILE_PROCESSING_ERROR, key, bucketName));
        }
    }

    private void checkFileCommand(final String bucketName, final String key) {
        checkBucketExist(bucketName);
        if (!amazonS3.doesObjectExist(bucketName, key)) {
            throw new DefaultFrameworkException(String.format(FILE_NOT_FOUND, key, bucketName));
        }
    }

    private void removeFile(final S3File fileCommand,
                            final String bucketName,
                            final String key,
                            final CommandResult result) {
        checkFileCommand(bucketName, key);
        amazonS3.deleteObject(bucketName, fileCommand.getRemove());
        compareFileActionResult(fileCommand.getExpected(), bucketName, result);
    }
}
