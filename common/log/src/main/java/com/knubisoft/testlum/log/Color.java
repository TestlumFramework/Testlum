package com.knubisoft.testlum.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Color {

    RED("\u001B[31m"),
    ORANGE("\u001b[38;5;208m"),
    GREEN("\u001B[32m"),
    CYAN("\u001b[36m"),
    RESET("\u001b[0m"),
    YELLOW("\u001B[33m"),
    NONE("");

    private final String code;
}
