package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.framework.util.ResultUtil;
import com.knubisoft.e2e.testing.model.scenario.Shell;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.CONTENT_FORMAT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.REGEX_NEW_LINE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SHELL_COMMAND_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SHELL_FILE_LOG;

@Slf4j
@InterpreterForClass(Shell.class)
public class ShellInterpreter extends AbstractInterpreter<Shell> {

    private static final String EXEC_WINDOWS_COMMAND = "cmd.exe /c dir %s";
    private static final String EXEC_LINUX_COMMAND = "sh %s";
    private static final String EXPECTED_RESULT = "{\"expectedCode\": %d}";

    public ShellInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    @SneakyThrows
    protected void acceptImpl(final Shell shell, final CommandResult result) {
        List<String> shellCommands = shell.getShellCommand();
        List<String> shellFiles = shell.getShellFile();

        ResultUtil.addShellMetaData(shellFiles, shellCommands, result);

        execShellCommand(shellCommands, shell, result);
        execShellFiles(shellFiles, shell, result);
    }

    private void execShellCommand(final List<String> shellCommands, final Shell shell,
                                  final CommandResult result) {
        shellCommands.forEach(shellCommand -> {
            execShellCommandOrThrow(shellCommand, shell, result);
        });
    }

    private void execShellCommandOrThrow(final String shellCommand, final Shell shell,
                                  final CommandResult result) {
        try {
            log.info(SHELL_COMMAND_LOG, shellCommand);
            Process process = Runtime.getRuntime().exec(shellCommand);
            processExpectedAndActual(process.waitFor(), shell, result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void execShellFiles(final List<String> shellFiles, final Shell shell,
                                final CommandResult result) {
        shellFiles.forEach(shellFile -> {
            execShellFileOrThrow(shellFile, shell, result);
        });
    }

    private void execShellFileOrThrow(final String shellFile, final Shell shell,
                                      final CommandResult result) {
        try {
            Process process = getProcessorForFile(shellFile);
            processExpectedAndActual(process.waitFor(), shell, result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Process getProcessorForFile(final String shellFile) throws IOException {
        File shellFileByPath = getShellFileByPath(shellFile);
        log.info(SHELL_FILE_LOG, new String(Files.readAllBytes(shellFileByPath.toPath()), StandardCharsets.UTF_8)
                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        return SystemUtils.IS_OS_WINDOWS
                ? Runtime.getRuntime().exec(String.format(EXEC_WINDOWS_COMMAND,
                shellFileByPath.getAbsolutePath()))
                : Runtime.getRuntime().exec(String.format(EXEC_LINUX_COMMAND,
                shellFileByPath.getAbsolutePath()));
    }

    private File getShellFileByPath(final String filePath) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        return fileSearcher.fileByNameAndExtension(filePath);
    }

    private void processExpectedAndActual(final int expectedCode, final Shell shell,
                                          final CommandResult result) {
        String actual = PrettifyStringJson.getJSONResult(String.format(EXPECTED_RESULT, expectedCode));
        String expected = PrettifyStringJson.getJSONResult(getContentIfFile(shell.getFile()));
        CompareBuilder compare = newCompare()
            .withActual(actual)
            .withExpected(expected);
        compare.exec();

        result.setActual(actual);
        result.setExpected(expected);
    }
}
