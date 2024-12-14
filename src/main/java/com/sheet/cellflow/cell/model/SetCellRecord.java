package com.sheet.cellflow.cell.model;

import com.sheet.cellflow.column.model.ColumnType;

public record SetCellRecord(
    String columnId,
    Integer rowIndex,
    Boolean isWithLookup,
    String cellValue,
    ColumnType columnType
) {

}
