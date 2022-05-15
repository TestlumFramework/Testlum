package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.model.scenario.Shell;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

        execShellCommand(shellCommands, shell);
        execShellFiles(shellFiles, shell);
    }

    private void execShellCommand(final List<String> shellCommands, final Shell shell) {
        shellCommands.forEach(shellCommand -> {
            try {
                Process process = Runtime.getRuntime().exec(shellCommand);
                execShell(process, shell);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void execShellFiles(final List<String> shellFiles, final Shell shell) {
        shellFiles.forEach(shellFile -> {
            try {
                Process process = getProcessorForFile(shellFile);
                execShell(process, shell);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void execShell(final Process process, final Shell shell) throws InterruptedException {
        StreamHelper streamHelper = new StreamHelper(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamHelper);
        processExpectedAndActual(process.waitFor(), shell);
    }

    private Process getProcessorForFile(final String shellFile) throws IOException {
        File shellFileByPath = getShellFileByPath(shellFile);
        return SystemUtils.IS_OS_WINDOWS
                ? Runtime.getRuntime().exec(String.format(EXEC_WINDOWS_COMMAND,
                shellFileByPath.getAbsolutePath()))
                : Runtime.getRuntime().exec(String.format(EXEC_LINUX_COMMAND,
                shellFileByPath.getAbsolutePath()));
    }

    private File getShellFileByPath(final String filePath) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        File shellFolder = TestResourceSettings.getInstance().getShellFolder();
        return fileSearcher.search(shellFolder, filePath);
    }

    private void processExpectedAndActual(final int expectedCode, final Shell shell) {
        CompareBuilder compare = newCompare()
            .withActual(PrettifyStringJson.getJSONResult(
                    String.format(EXPECTED_RESULT, expectedCode)))
            .withExpectedFile(shell.getFile());
        compare.exec();
    }

    private static class StreamHelper implements Runnable {

        private final InputStream inputStream;
        private final Consumer<String> consumer;

        StreamHelper(final InputStream inputStream, final Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}
