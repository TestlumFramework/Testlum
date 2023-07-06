package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.S3;
import com.knubisoft.testlum.testing.model.scenario.S3File;

public class S3Util {
    public static final String CREATE_BUCKET = "create bucket";
    public static final String REMOVE_BUCKET = "remove bucket";
    public static final String UPLOAD_FILE = "upload file";
    public static final String DOWNLOAD_FILE = "download file";
    public static final String REMOVE_FILE = "remove file";

    public static void logAndReportAlias(final S3 s3, final CommandResult result) {
        LogUtil.logAlias(s3.getAlias());
        ResultUtil.addAliasData(s3.getAlias(), result);
    }

    public static void logAndReportBucketInfo(final String bucketAcion,
                                              final String bucketName,
                                              final CommandResult result) {
        LogUtil.logS3BucketActionInfo(bucketAcion, bucketName);
        ResultUtil.addS3BucketMetaData(bucketAcion, bucketName, result);
    }

    public static void logAndReportFileInfo(final String commandType,
                                            final S3File fileCommand,
                                            final String fileName,
                                            final CommandResult result) {
        LogUtil.logS3FileActionInfo(commandType,
                fileCommand.getBucket(),
                fileCommand.getKey(),
                fileName);
        ResultUtil.addS3FileMetaData(commandType,
                fileCommand.getBucket(),
                fileCommand.getKey(),
                result);
    }
}
