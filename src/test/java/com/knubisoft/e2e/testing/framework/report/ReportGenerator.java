package com.knubisoft.e2e.testing.framework.report;

import com.google.gson.Gson;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings.REPORT_TEMPLATE_PATH;

public class ReportGenerator {

    @Autowired
    GlobalScenarioStatCollector globalScenarioStatCollector;

    @SneakyThrows
    public void generateReport() {
        File testResourcesFolder = TestResourceSettings.getInstance().getTestResourcesFolder();
        String reportPath = testResourcesFolder + TestResourceSettings.REPORT_PATH;
        Files.deleteIfExists(Paths.get(reportPath));
        replaceData(reportPath);
    }

    @SneakyThrows
    private void replaceData(final String reportPath) {
        InputStream reportTemplate = new ClassPathResource(REPORT_TEMPLATE_PATH).getInputStream();
        String htmlString = IOUtils.toString(reportTemplate, StandardCharsets.UTF_8)
                .replace("$json", getJSONResult());
        FileUtils.writeStringToFile(new File(reportPath), htmlString, "UTF-8");
    }

    @SneakyThrows
    private String getJSONResult() {
        String json = new Gson().toJson(getResult());
        return PrettifyStringJson.getJSONResult(json);
    }

    private ReportResult getResult() {
        List<ScenarioResult> scenarios = globalScenarioStatCollector.getResults();
        ReportResult result = new ReportResult();
        result.setGlobalTestConfiguration(GlobalTestConfigurationProvider.provide());
        result.setAggregatedReport(getAggregatedReport(scenarios));
        result.setScenarios(scenarios);
        return result;
    }

    //CHECKSTYLE:OFF
    @SneakyThrows
    private AggregatedReport getAggregatedReport(final List<ScenarioResult> scenarios) {
        Path path = Paths.get(System.getProperty("user.dir")).getFileName();
        AggregatedReport aggregatedReport = new AggregatedReport();
        aggregatedReport.setProjectName(String.valueOf(path));
        aggregatedReport.setBranch(getCurrentGitBranch());
        aggregatedReport.setReportDate(LocalDateTime.now(ZoneOffset.UTC).toString());
        aggregatedReport.setTotalScenarios(scenarios.size());
        setScenariosCounters(aggregatedReport, scenarios);
        //TODO implement
        aggregatedReport.setCoverage(0);
        return aggregatedReport;
    }

    //CHECKSTYLE:ON
    private String getCurrentGitBranch() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
        process.waitFor();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                StandardCharsets.UTF_8))) {
            return reader.readLine();
        }
    }

    private void setScenariosCounters(final AggregatedReport aggregatedReport, final List<ScenarioResult> scenarios) {
        long goodScenariosCounter = statusScenariosCounter(scenarios, true);
        long badScenariosCounter = statusScenariosCounter(scenarios, false);
        aggregatedReport.setTotalSuccessScenarios(goodScenariosCounter);
        aggregatedReport.setTotalFailedScenarios(badScenariosCounter);
        aggregatedReport.setAllTestsPassed(badScenariosCounter == 0L);
    }

    private long statusScenariosCounter(final List<ScenarioResult> scenarios, final boolean b) {
        List<Boolean> collect = scenarios.stream().map(ScenarioResult::isSuccess)
                .collect(Collectors.toList());
        return collect.stream().filter(o -> o.equals(b)).count();
    }
}
