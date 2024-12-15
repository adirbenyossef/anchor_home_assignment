package com.sheet.cellflow.column;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.service.CellService;
import com.sheet.cellflow.column.dto.ColumnRequestDto;
import com.sheet.cellflow.column.dto.ColumnResponseDto;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.column.repository.ColumnRepository;
import com.sheet.cellflow.column.service.ColumnService;
import com.sheet.cellflow.exception.SheetOperationException;

@ExtendWith(MockitoExtension.class)
class ColumnServiceTest {
    @Mock
    private ColumnRepository columnRepo;

    @Mock
    private CellService cellService;

    @InjectMocks
    private ColumnService columnService;

    @Test
    void testGetColumnSuccess() {
        List<CellResponseDto> cells = generateMockCells();
        List<Column> columnRecords = generateMockColumnRecords();
        List<ColumnResponseDto> expected = generateColumnResponseDTO(columnRecords, cells);

        when(cellService.filterByColumnId("col-id")).thenReturn(cells);
        when(columnRepo.findBySheetId("sheet-id")).thenReturn(columnRecords);

        List<ColumnResponseDto> response = columnService.filterBySheetId("sheet-id");
        assertEquals(expected, response);
    }

    @Test
    void testCreateColumnSuccess() {
        List<ColumnRequestDto> columns = new ArrayList<>();
        ColumnRequestDto column = new ColumnRequestDto("A", "STRING");
        columns.add(column);
        List<Column> expected = generateColumnEntities(columns);

        List<Column> response = columnService.setColumns(columns, "sheet-id");

        assertEquals(expected.getFirst().getName(), response.getFirst().getName());
        assertEquals(expected.getFirst().getSheetId(), response.getFirst().getSheetId());
        assertEquals(expected.getFirst().getColumnType(), response.getFirst().getColumnType());
    }

    @Test
    void testCreateColumnWithInvalidType() {
        List<ColumnRequestDto> columns = new ArrayList<>();
        ColumnRequestDto column = new ColumnRequestDto("A", "INVALID_TYPE");
        columns.add(column);
        
        assertThrows(SheetOperationException.class, () -> {
            columnService.setColumns(columns, "sheet-id");
        });
    }

    private List<Column> generateColumnEntities(List<ColumnRequestDto> columns) {
        List<Column> result = new ArrayList<>();
        ColumnRequestDto col = columns.getFirst();
        Column entity = new Column("col-id", "sheet-id", col.name(), ColumnType.valueOf(col.type()));
        result.add(entity);
        return result;
    }

    private List<CellResponseDto> generateMockCells() {
        CellResponseDto cell = new CellResponseDto("cel-id", 1, "adir", "col-id", false, null);
        List<CellResponseDto> cells = new ArrayList<>();
        cells.add(cell);
        return cells;
    }

    private List<Column> generateMockColumnRecords() {
        List<Column> columnRecords = new ArrayList<>();
        Column col = new Column("col-id", "sheet-id", "test", ColumnType.STRING);
        columnRecords.add(col);
        return columnRecords;
    }

    private List<ColumnResponseDto> generateColumnResponseDTO(List<Column> columnRecords, List<CellResponseDto> cells) {
        List<ColumnResponseDto> expected = new ArrayList<>(); 
        Column col = columnRecords.getFirst();
        ColumnResponseDto colRes = new ColumnResponseDto(col.getId(), col.getName(), col.getColumnType(),col.getSheetId(), cells);
        expected.add(colRes);  
        return expected;
     }
}
