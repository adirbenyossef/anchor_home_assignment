package com.sheet.cellflow.cell.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cell")
public class Cell {
    @Id
    private String id;
    private String columnId;
    private Integer rowIndex;
    private Boolean isWithLookup;
    private String cellValue;

    public Cell() {}

    public Cell(String id, String cellValue, String columnId, Integer rowIndex, Boolean isWithLookup) {
        this.id = id;
        this.cellValue = cellValue;
        this.columnId = columnId;
        this.rowIndex = rowIndex;
        this.isWithLookup = isWithLookup;
    }

    public Cell setId(String id) {
        this.id = id;
        return this;
    }

    public Cell setCellValue(String cellValue) {
        this.cellValue = cellValue;
        return this;
    }
    
    public Cell setColumnId(String columnId) {
        this.columnId = columnId;
        return this;
    }

    public Cell setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    public Cell setIsWithLookup(boolean isWithLookup) {
        this.isWithLookup = isWithLookup;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public String getCellValue() {
        return this.cellValue;
    }

    public String getColumnId() {
        return this.columnId;
    }

    public Integer getRowIndex() {
        return this.rowIndex;
    }

    public Boolean getIsWithLookup() {
        return this.isWithLookup;
    }
}
