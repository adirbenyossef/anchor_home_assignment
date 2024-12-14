package com.sheet.cellflow.cell.dto;

public record CreateCellRequestDto(
    String columnName,
    Integer rowIndex,
    Object value
) {

}
