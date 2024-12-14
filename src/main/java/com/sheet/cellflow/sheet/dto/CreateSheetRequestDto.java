package com.sheet.cellflow.sheet.dto;

import java.util.List;

import com.sheet.cellflow.column.dto.ColumnRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSheetRequestDto (
    @NotNull
    @Size(min = 1)
    @Valid
    List<ColumnRequestDto> columns    
) {

}
