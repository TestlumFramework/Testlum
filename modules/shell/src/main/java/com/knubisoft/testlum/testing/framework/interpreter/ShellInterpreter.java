package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Shell;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@InterpreterForClass(Shell.class)
public class ShellInterpreter extends AbstractInterpreter<Shell> {

    private static final String[] EXEC_WINDOWS_COMMAND = {"cmd.exe", "/c"};
    private static final String EXEC_LINUX_COMMAND = "sh";
    private static final String EXPECTED_RESULT = "{\"expectedCode\": %d}";

    private static final String SHELL_FILES = "Shell files";
    private static final String SHELL_COMMANDS = "Shell commands";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String SHELL_COMMAND_LOG = format(TABLE_FORMAT, "Shell command", "{}");
    private static final String SHELL_FILE_LOG = format(TABLE_FORMAT, "Shell file", "{}");
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);

    public ShellInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    @SneakyThrows
    protected void acceptImpl(final Shell o, final CommandResult result) {
        Shell shell = injectCommand(o);
        List<String> shellCommands = shell.getShellCommand();
        List<String> shellFiles = shell.getShellFile();

        addShellMetaData(shellFiles, shellCommands, result);

        execShellCommand(shellCommands, shell, result);
        execShellFiles(shellFiles, shell, result);
    }

    private void execShellCommand(final List<String> shellCommands, final Shell shell, final CommandResult result) {
        shellCommands.forEach(shellCommand -> execShellCommandOrThrow(shellCommand, shell, result));
    }

    private void execShellCommandOrThrow(final String shellCommand, final Shell shell, final CommandResult result) {
        try {
            log.info(SHELL_COMMAND_LOG, shellCommand);
            Process process = Runtime.getRuntime().exec(shellCommand);
            processExpectedAndActual(process.waitFor(), shell, result);
        } catch (IOException | InterruptedException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    private void execShellFiles(final List<String> shellFiles, final Shell shell, final CommandResult result) {
        shellFiles.forEach(shellFileCommand -> execShellFileOrThrow(shellFileCommand, shell, result));
    }

    private void execShellFileOrThrow(final String shellFileCommand, final Shell shell, final CommandResult result) {
        try {
            Process process = getProcessorForFile(shellFileCommand);
            processExpectedAndActual(process.waitFor(), shell, result);
        } catch (IOException | InterruptedException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    private Process getProcessorForFile(final String shellFileCommand) throws IOException {
        String[] shellFileCommandParts = shellFileCommand.trim().split("\\s+");
        File shellFileByPath = FileSearcher.searchFileFromDataFolder(shellFileCommandParts[0]);

        log.info(SHELL_COMMAND_LOG, shellFileCommand);

        List<String> commandList = computeCommandList(shellFileByPath, shellFileCommandParts);
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.directory(shellFileByPath.getParentFile());
        return pb.start();
    }

    private List<String> computeCommandList(final File shellFileByPath, final String[] shellFileCommandParts) {
        List<String> commandList = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            commandList.addAll(Arrays.asList(EXEC_WINDOWS_COMMAND));
        } else {
            commandList.add(EXEC_LINUX_COMMAND);
        }
        commandList.add(shellFileByPath.getName());
        commandList.addAll(Arrays.asList(shellFileCommandParts).subList(1, shellFileCommandParts.length));
        return commandList;
    }

    private void processExpectedAndActual(final int expectedCode, final Shell shell, final CommandResult result) {
        String actual = String.format(EXPECTED_RESULT, expectedCode);

        CompareBuilder compare = newCompare()
                .withActual(actual)
                .withExpectedFile(shell.getFile());

        result.setActual(StringPrettifier.asJsonResult(actual));
        result.setExpected(StringPrettifier.asJsonResult(compare.getExpected()));
        compare.exec();
    }

    private void addShellMetaData(final List<String> shellFiles,
                                  final List<String> shellCommands,
                                  final CommandResult result) {
        if (!shellCommands.isEmpty()) {
            result.put(SHELL_COMMANDS, shellCommands);
        }
        if (!shellFiles.isEmpty()) {
            result.put(SHELL_FILES, shellFiles);
        }
    }
}
