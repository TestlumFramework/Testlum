package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.client.ReportPublisher;
import com.testlum.reporting.sdk.client.TestlumReportClient;
import com.testlum.reporting.sdk.model.launch.LaunchFinished;
import com.testlum.reporting.sdk.model.launch.LaunchStarted;
import com.testlum.reporting.sdk.model.launch.LaunchStatus;
import com.testlum.testing.model.global_config.Environment;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.TagValue;
import com.testlum.testing.report.GlobalScenarioStatCollector;
import com.testlum.testing.report.ReportListener;
import com.testlum.testing.report.ScenarioResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Lazy
@Component
public class TestlumServerReportListener implements ReportListener {

    private static final long DRAIN_TIMEOUT_SECONDS = 60;
    private static final long INTERRUPTED_TIMEOUT_SECONDS = 5;

    private final GlobalTestConfiguration globalTestConfiguration;
    private final ScenarioReportMapper scenarioReportMapper;
    private final ProjectConfigSnapshotMapper projectConfigSnapshotMapper;
    private final TestlumReportClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(this::publisherThread);
    private final Thread shutdownHook = new Thread(this::onInterrupted, "testlum-report-interrupted");
    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();
    private final AtomicInteger submitted = new AtomicInteger();
    private final AtomicInteger undelivered = new AtomicInteger();
    private final AtomicBoolean finished = new AtomicBoolean();
    private volatile ReportPublisher publisher;
    private volatile long startedAt;

    public TestlumServerReportListener(final GlobalTestConfiguration globalTestConfiguration,
                                       final TestlumReportClientFactory clientFactory,
                                       final ScenarioReportMapper scenarioReportMapper,
                                       final ProjectConfigSnapshotMapper projectConfigSnapshotMapper) {
        this.globalTestConfiguration = globalTestConfiguration;
        this.scenarioReportMapper = scenarioReportMapper;
        this.projectConfigSnapshotMapper = projectConfigSnapshotMapper;
        this.client = clientFactory.create(globalTestConfiguration.getReport());
    }

    @Override
    public void onRunStart() {
        startedAt = System.currentTimeMillis();
        publisher = client.openPublisher();
        log.info("Reporting launch {} to Testlum report server {}", publisher.getLaunchId(), client.getReportingUrl());
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        submit("launch start", () -> publisher.publishLaunchStarted(launchStarted()));
        submit("project config", () -> publisher.publishProjectConfig(projectConfigSnapshotMapper.map()));
    }

    @Override
    public void onScenarioFinished(final ScenarioResult result) {
        counterOf(result).incrementAndGet();
        if (counterOf(result) == passed && globalTestConfiguration.getReport().isOnlyFailedScenarios()) {
            return;
        }
        submit("scenario " + result.getPath(), () -> publisher.publishScenario(scenarioReportMapper.map(result)));
    }

    @Override
    public void onRunFinished(final GlobalScenarioStatCollector collector) {
        if (publisher == null || !finished.compareAndSet(false, true)) {
            return;
        }
        removeShutdownHook();
        LaunchStatus status = failed.get() > 0 ? LaunchStatus.FAILED : LaunchStatus.PASSED;
        submit("launch finish", () -> publisher.publishLaunchFinished(launchFinished(status)));
        drain();
        closePublisher();
        logSummary();
    }

    void onInterrupted() {
        if (publisher == null || !finished.compareAndSet(false, true)) {
            return;
        }
        executor.shutdownNow();
        try {
            CompletableFuture.runAsync(() -> publisher.publishLaunchFinished(launchFinished(LaunchStatus.INTERRUPTED)))
                    .get(INTERRUPTED_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Could not report the interrupted launch {} to the Testlum report server",
                    publisher.getLaunchId(), e);
        }
    }

    private AtomicInteger counterOf(final ScenarioResult result) {
        if (result.isSkipped()) {
            return skipped;
        }
        return result.isSuccess() ? passed : failed;
    }

    private void submit(final String description, final Runnable publish) {
        submitted.incrementAndGet();
        try {
            executor.execute(() -> publishSafely(description, publish));
        } catch (RejectedExecutionException e) {
            undelivered.incrementAndGet();
            log.warn("Skipped reporting {} to the Testlum report server: the launch is already closed", description);
        }
    }

    private void publishSafely(final String description, final Runnable publish) {
        try {
            publish.run();
        } catch (Exception e) {
            undelivered.incrementAndGet();
            log.warn("Could not report {} to the Testlum report server: {}", description, e.getMessage(), e);
        }
    }

    private void drain() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                int dropped = executor.shutdownNow().size();
                undelivered.addAndGet(dropped);
                log.warn("Testlum report server did not accept the report within {}s, {} message(s) dropped",
                        DRAIN_TIMEOUT_SECONDS, dropped);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private void closePublisher() {
        try {
            publisher.close();
        } catch (Exception e) {
            log.warn("Could not close the connection to the Testlum report server: {}", e.getMessage());
        }
    }

    private void logSummary() {
        if (undelivered.get() > 0) {
            log.warn("{} of {} report message(s) of launch {} were not delivered to the Testlum report server",
                    undelivered.get(), submitted.get(), publisher.getLaunchId());
        } else {
            log.info("Launch {} reported to Testlum report server {}", publisher.getLaunchId(),
                    client.getReportingUrl());
        }
    }

    private void removeShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            log.debug("JVM is already shutting down, shutdown hook stays registered");
        }
    }

    private LaunchStarted launchStarted() {
        return LaunchStarted.builder()
                .startedAt(startedAt)
                .hostName(hostName())
                .testlumVersion(getClass().getPackage().getImplementationVersion())
                .environments(enabledEnvironments())
                .tags(enabledTags())
                .build();
    }

    private LaunchFinished launchFinished(final LaunchStatus status) {
        long finishedAt = System.currentTimeMillis();
        return LaunchFinished.builder()
                .status(status)
                .total(passed.get() + failed.get() + skipped.get())
                .passed(passed.get())
                .failed(failed.get())
                .skipped(skipped.get())
                .finishedAt(finishedAt)
                .durationMs(finishedAt - startedAt)
                .build();
    }

    private List<String> enabledEnvironments() {
        if (globalTestConfiguration.getEnvironments() == null) {
            return List.of();
        }
        return globalTestConfiguration.getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled)
                .map(Environment::getFolder)
                .toList();
    }

    private List<String> enabledTags() {
        RunScenariosByTag byTag = globalTestConfiguration.getRunScenariosByTag();
        if (byTag == null || !byTag.isEnabled()) {
            return null;
        }
        return byTag.getTag().stream().filter(TagValue::isEnabled).map(TagValue::getName).toList();
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private Thread publisherThread(final Runnable runnable) {
        Thread thread = new Thread(runnable, "testlum-report-publisher");
        thread.setDaemon(true);
        return thread;
    }
}
