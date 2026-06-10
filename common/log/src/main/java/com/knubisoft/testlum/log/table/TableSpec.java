package com.knubisoft.testlum.log.table;

import com.knubisoft.testlum.log.Color;

import java.util.List;

record TableSpec(Caption title,
                 String[] headers,
                 List<Row> rows,
                 Caption footer,
                 Align align,
                 Color color) {
}
