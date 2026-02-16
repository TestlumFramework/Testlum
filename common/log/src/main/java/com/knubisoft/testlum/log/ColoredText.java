package com.knubisoft.testlum.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ColoredText {

    private final List<String> lines = new ArrayList<>();
    private final Color color;

    public void addAll(final List<String> data) {
        if (data != null) {
            for (String o : data) {
                add(o);
            }
        }
    }

    public void add(final String msg) {
        if (msg != null) {
            lines.add(msg);
        }
    }

    public void info() {
        print(log::info);
    }

    private void print(final Consumer<String> consumer) {
        for (String line : lines) {
            consumer.accept(LogFormat.with(color, line));
        }
        lines.clear();
    }
}