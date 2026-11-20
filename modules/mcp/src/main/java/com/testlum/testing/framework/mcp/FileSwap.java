package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.constant.ToolMessage;
import com.testlum.testing.framework.mcp.exception.ToolException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Puts a set of files in place together. Each new file is written beside its target first, each file that is
 * being replaced is copied to a sibling backup, and only then are the new files moved in. If any step fails the
 * originals are put back and the files this call created are removed. A backup is only deleted once its original
 * is back; if the restore fails the backup stays and is named. Restore and cleanup failures are kept as suppressed
 * exceptions on the failure that caused the rollback.
 */
final class FileSwap {

    /** Moves a file into place; a seam so a test can make one move fail. */
    @FunctionalInterface
    interface Mover {
        void move(Path from, Path to) throws IOException;
    }

    private static final Mover REPLACING = (from, to) -> Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);

    private final Mover mover;
    private final List<Step> steps = new ArrayList<>();
    private final List<Path> stuck = new ArrayList<>();
    private final List<Path> kept = new ArrayList<>();
    private final List<IOException> problems = new ArrayList<>();

    private FileSwap(final Mover mover) {
        this.mover = mover;
    }

    /** One target: its staged text, the backup of what it replaces (if anything) and whether it was moved in. */
    private static final class Step {
        private final Path target;
        private Path staged;
        private Path backup;
        private boolean moved;

        private Step(final Path target) {
            this.target = target;
        }
    }

    static void apply(final Map<Path, String> content) {
        apply(content, REPLACING);
    }

    static void apply(final Map<Path, String> content, final Mover mover) {
        new FileSwap(mover).run(content);
    }

    private void run(final Map<Path, String> content) {
        try {
            for (var e : content.entrySet()) {
                stage(e.getKey(), e.getValue());
            }
            for (Step step : steps) {
                moveIn(step);
            }
        } catch (IOException ex) {
            rollBack();
            problems.forEach(ex::addSuppressed);
            throw new ToolException(ex, ToolMessage.CANNOT_WRITE, "scenario files", ex.getMessage() + leftovers());
        }
        finish();
    }

    private void finish() {
        steps.forEach(s -> remove(s.backup));
        if (!stuck.isEmpty()) {
            ToolException failure = new ToolException(ToolMessage.CANNOT_CLEAN_UP, stuck);
            problems.forEach(failure::addSuppressed);
            throw failure;
        }
    }

    private void stage(final Path target, final String text) throws IOException {
        Step step = new Step(target);
        steps.add(step);
        Files.createDirectories(target.getParent());
        step.staged = Files.createTempFile(target.getParent(), ".testlum-", ".tmp");
        Files.writeString(step.staged, text, StandardCharsets.UTF_8);
    }

    private void moveIn(final Step step) throws IOException {
        if (Files.exists(step.target)) {
            step.backup = Files.createTempFile(step.target.getParent(), ".testlum-", ".bak");
            Files.copy(step.target, step.backup, StandardCopyOption.REPLACE_EXISTING);
        }
        mover.move(step.staged, step.target);
        step.moved = true;
    }

    private void rollBack() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            Step step = steps.get(i);
            boolean restored = restore(step);
            remove(step.staged);
            if (restored) {
                remove(step.backup);
            } else if (step.backup != null) {
                kept.add(step.backup);
            }
        }
    }

    /** Only a target that was moved in has changed: it gets its backup back, or goes if it is new. */
    private boolean restore(final Step step) {
        try {
            if (step.moved) {
                undo(step);
            }
            return true;
        } catch (IOException e) {
            stuck.add(step.target);
            problems.add(e);
            return false;
        }
    }

    private void undo(final Step step) throws IOException {
        if (step.backup != null) {
            mover.move(step.backup, step.target);
        } else {
            Files.deleteIfExists(step.target);
        }
    }

    private void remove(final Path file) {
        try {
            if (file != null) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            stuck.add(file);
            problems.add(e);
        }
    }

    private String leftovers() {
        String left = stuck.isEmpty() ? "" : " (could not clean up: " + stuck + ")";
        return kept.isEmpty() ? left : left + " (originals kept at: " + kept + ")";
    }
}
