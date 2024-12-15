package com.sheet.cellflow.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.column.dto.ColumnRequestDto;
import com.sheet.cellflow.column.dto.ColumnResponseDto;
import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.exception.SheetOperationException;
import com.sheet.cellflow.sheet.controller.SheetController;
import com.sheet.cellflow.sheet.dto.CreateSheetRequestDto;
import com.sheet.cellflow.sheet.dto.SheetResponseDto;
import com.sheet.cellflow.sheet.service.SheetService;

@ExtendWith(MockitoExtension.class)
class SheetControllerTest {
    @Mock
    private SheetService sheetService;

    @InjectMocks
    private SheetController sheetController;

    @Test
    void testGetSheetSuccess() {
        SheetResponseDto mockSheet = generateMockSheet();
            
        when(sheetService.find("sheet-id")).thenReturn(Optional.of(mockSheet));

        ResponseEntity<SheetResponseDto>  response = sheetController.getSheet("sheet-id");
        SheetResponseDto res = response.getBody();
        assertEquals(mockSheet, res);
    }


    @Test
    void testCreateSheetSuccess() {
        List<ColumnRequestDto> columns = new ArrayList<>();
        ColumnRequestDto column = new ColumnRequestDto("A", "STRING");
        columns.add(column);
        CreateSheetRequestDto request = new CreateSheetRequestDto(columns);

        when(sheetService.setSheet(columns)).thenReturn("abc123");

        ResponseEntity<String> response = sheetController.createSheet(request);
        assertEquals("abc123", response.getBody());
    }

    @Test
    void testCreateSheetFailed() {
        List<ColumnRequestDto> columns = new ArrayList<>();
        ColumnRequestDto column = new ColumnRequestDto("A", "OBJ");
        columns.add(column);
        CreateSheetRequestDto request = new CreateSheetRequestDto(columns);

        when(sheetService.setSheet(columns))
            .thenThrow(new SheetOperationException("Invalid column type: OBJ"));

        ResponseEntity<String> response = sheetController.createSheet(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid column type: OBJ", response.getBody());
    }

    private SheetResponseDto generateMockSheet() {
        CellResponseDto cell = new CellResponseDto("cell-id", 1, "test", "col-id", false, null);
        List<CellResponseDto> cells = new ArrayList<>();
        cells.add(cell);
        ColumnResponseDto column = new ColumnResponseDto("col-id","col name",ColumnType.STRING, "sheet-id", cells);
        List<ColumnResponseDto> columns = new ArrayList<>();
        columns.add(column);
        return new SheetResponseDto("sheet-id", columns);
    }
}
