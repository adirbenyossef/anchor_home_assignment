package com.sheet.cellflow.lookup.model;

import java.util.Objects;

import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.exception.ExceptionMapper;

public class Lookup {
    private final String columnIdentifier; //could be columnName for POST input DTO but columnId for GET input DTO
    private final int rowIndex;
    private String targetCellValue;
    private ColumnType targetColumnType;

    public Lookup(Object value){
        String val = value.toString();
        String[] parts = val.split(",");
        String rowIndexString = parts[1].substring(0, parts[1].length() - 1);
        this.columnIdentifier = parts[0].substring(7);
        this.rowIndex = Integer.parseInt(rowIndexString);
        Objects.requireNonNull(columnIdentifier, ExceptionMapper.NULL_COLUMN_ID.getError());
        if (rowIndex < 1) {
            throw new IllegalArgumentException(ExceptionMapper.NEGATIVE_TARGET_ROW.getError());
        }
    }

    public String getColumnIdentifier() {
        return columnIdentifier;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public String getTargetValue() {
        return this.targetCellValue;
    }

    public ColumnType getColumnType() {
        return this.targetColumnType;
    }

    public void setValue(String cellValue) {
        this.targetCellValue = cellValue;
    }

    public void setColumnType(ColumnType columnType) {
        this.targetColumnType = columnType;
    }
}
