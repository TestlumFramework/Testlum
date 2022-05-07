package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.interpreter.formater.CsvFormatter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.model.scenario.ExcelCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

@Slf4j
@InterpreterForClass(ExcelCommands.class)
public class ExcelInterpreter extends AbstractInterpreter<ExcelCommands> {

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public ExcelInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final ExcelCommands excelCommands, final CommandResult result) {
        excelCommands.getExcelFile().forEach(excelFile -> {
            File excel = getCsvFileByPath(excelFile);
            List<String> commands = CsvFormatter.convertFileToString(excel);
            Source source = CsvFormatter.getSource(commands);
            postgresSqlOperation.apply(source, excelCommands.getAlias());
        });
    }

    public File getCsvFileByPath(final String pathToFile) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        File excelFolder = TestResourceSettings.getInstance().getExcelFolder();
        return fileSearcher.search(excelFolder, pathToFile);
    }
}
