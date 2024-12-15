package com.sheet.cellflow.cell.dto;

public record CellResponseDto(
    String id,
    Integer rowIndex,
    Object value,
    String columnId,
    boolean isWithLookup,
    String error
) {

}
