package com.sheet.cellflow.column.model;

import java.util.Objects;

import com.sheet.cellflow.exception.ExceptionMapper;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "column_table")
public class Column {
    @Id
    String id;
    String sheetId;
    String name;
    @Enumerated(EnumType.STRING)
    ColumnType columnType;

    
    public Column (){}

    public Column (
        String id,
        String sheetId,
        String name,
        ColumnType columnType
    ){
        Objects.requireNonNull(name, ExceptionMapper.COLUMN_NAME_NULL.getError());
        Objects.requireNonNull(columnType, ExceptionMapper.COLUMN_TYPE_NULL.getError());
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException(ExceptionMapper.COLUMN_NAME_EMPTY.getError());
        }
        this.id = id;
        this.name = name;
        this.sheetId = sheetId;
        this.columnType = columnType;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getSheetId() {
        return this.sheetId;
    }

    public ColumnType getColumnType() {
        return this.columnType;
    }
}
