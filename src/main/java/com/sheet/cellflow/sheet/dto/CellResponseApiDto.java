package com.sheet.cellflow.sheet.dto;

import java.util.List;

import com.sheet.cellflow.cell.dto.CellResponseDto;

import jakarta.annotation.Nullable;

public record CellResponseApiDto (
    @Nullable
    List<CellResponseDto> cells,
    @Nullable
    String error
) {

}
