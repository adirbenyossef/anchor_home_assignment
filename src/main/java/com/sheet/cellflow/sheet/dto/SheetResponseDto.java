package com.sheet.cellflow.sheet.dto;

import java.util.List;

import com.sheet.cellflow.column.dto.ColumnResponseDto;

public record SheetResponseDto(
    String id,
    List<ColumnResponseDto> columns
) {

}
