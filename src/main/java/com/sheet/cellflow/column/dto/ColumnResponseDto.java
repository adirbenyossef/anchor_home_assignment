package com.sheet.cellflow.column.dto;

import java.util.List;

import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.cell.dto.CellResponseDto;

public record ColumnResponseDto(
    String id,
    String name,
    ColumnType type,
    String sheetId,
    List<CellResponseDto> cells
) {

}
