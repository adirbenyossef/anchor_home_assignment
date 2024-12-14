package com.sheet.cellflow.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.column.dto.ColumnRequestDto;
import com.sheet.cellflow.column.dto.ColumnResponseDto;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.column.service.ColumnService;
import com.sheet.cellflow.sheet.dto.SheetResponseDto;
import com.sheet.cellflow.sheet.model.Sheet;
import com.sheet.cellflow.sheet.repository.SheetRepository;
import com.sheet.cellflow.sheet.service.SheetService;

@ExtendWith(MockitoExtension.class)
class SheetServiceTest {
    @Mock
    private SheetRepository sheetRepo;

    @Mock
    private ColumnService columnService;

    @InjectMocks
    private SheetService sheetService;

    @Test
    void testGetSheetSuccess() {
        List<ColumnResponseDto> columns = generateMockColumns();
        Optional<SheetResponseDto> expected = generateMockSheet(columns);

        when(sheetRepo.existsById("sheet-id")).thenReturn(true);
        when(columnService.filterBySheetId("sheet-id")).thenReturn(columns);
        
        Optional<SheetResponseDto> response = sheetService.find("sheet-id");
        assertEquals(expected, response);
    }

    @Test
    void testCreateSheetSuccess() {
        List<ColumnRequestDto> columns = new ArrayList<>();
        ColumnRequestDto column = new ColumnRequestDto("A", "STRING");
        columns.add(column);
        List<Column> cols = new ArrayList<>(); 
        Column columnEntity = new Column("col-id", "sheet-id", "A", ColumnType.STRING);
        cols.add(columnEntity);

        when(sheetRepo.save(any(Sheet.class))).thenReturn(new Sheet());
        when(columnService.setColumns(eq(columns), any(String.class))).thenReturn(cols);

        String response = sheetService.setSheet(columns);

        assertNotNull(response);
        verify(sheetRepo).save(any(Sheet.class));
        verify(columnService).setColumns(eq(columns), eq(response));
    }

    private List<ColumnResponseDto> generateMockColumns() {
        CellResponseDto cell = new CellResponseDto("cel-id",1, "adir", "col-id", false);
        List<CellResponseDto> cells = new ArrayList<>();
        cells.add(cell);
        ColumnResponseDto column = new ColumnResponseDto("col-id", "col-name", ColumnType.STRING,"sheet-id", cells);
        List<ColumnResponseDto> columns = new ArrayList<>();
        columns.add(column);
        return columns;
    }

    private Optional<SheetResponseDto> generateMockSheet(List<ColumnResponseDto> columns) {
        return Optional.ofNullable(new SheetResponseDto("sheet-id", columns));
    }
}