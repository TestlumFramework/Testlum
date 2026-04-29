package com.knubisoft.testlum.log.table;

import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Align {

    LEFT(TextAlignment.LEFT),
    CENTER(TextAlignment.CENTER),
    RIGHT(TextAlignment.RIGHT);

    private final TextAlignment alignment;
}
