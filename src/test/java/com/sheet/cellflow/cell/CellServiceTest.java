package com.sheet.cellflow.cell;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.model.Cell;
import com.sheet.cellflow.cell.repository.CellRepository;
import com.sheet.cellflow.cell.service.CellService;
import com.sheet.cellflow.exception.ExceptionMapper;

@ExtendWith(MockitoExtension.class)
class CellServiceTest {
    @Mock
    private CellRepository cellRepo;


    @InjectMocks
    private CellService cellService;

    @Test
    void testGetCellSuccess() {
        List<Cell> cells = generateMockCells("cell value", 1, false);
        List<CellResponseDto> expected = generateCellResponseDTO(cells, cells.getFirst().getCellValue());

        when(cellRepo.findAllByColumnId("col-id")).thenReturn(cells);

        List<CellResponseDto> response = cellService.filterByColumnId("col-id");
        assertEquals(expected, response);
    }

    @Test
    void testGetCellSuccessWithLookUp() {
        List<Cell> cells = generateMockCells("lookup(col-id,2)",1, true);
        List<Cell> expectedCells = generateMockCells("test row 2", 2, false);
        Cell lookupCell = expectedCells.getFirst();
        List<CellResponseDto> expected = generateCellResponseDTO(cells, expectedCells.getFirst().getCellValue());

        when(cellRepo.findAllByColumnId("col-id")).thenReturn(cells);
        when(cellRepo.findByColumnIdAndRowIndex("col-id", 2)).thenReturn(lookupCell);
        
        List<CellResponseDto> response = cellService.filterByColumnId("col-id");
        assertEquals(expected, response);
    }

    @Test
    void testGetCellSuccessWithDoubleLookUp() {
        List<Cell> cells = generateMockCells("lookup(col-id,2)",1, true);
        List<Cell> cells2 = generateMockCells("lookup(col-id,3)", 2, true);
        List<Cell> cells3 = generateMockCells("test row 3", 3, false);
        Cell cell1 = cells2.getFirst();
        Cell cell2 = cells3.getFirst();
        List<CellResponseDto> expected = generateCellResponseDTO(cells, cells3.getFirst().getCellValue());

        when(cellRepo.findAllByColumnId("col-id")).thenReturn(cells);
        when(cellRepo.findByColumnIdAndRowIndex("col-id", 2)).thenReturn(cell1);
        when(cellRepo.findByColumnIdAndRowIndex("col-id", 3)).thenReturn(cell2);

        List<CellResponseDto> response = cellService.filterByColumnId("col-id");
        assertEquals(expected, response);
    }

    @Test
    void testGetCellSuccessWithMaxLookUp() {
        int maxLookup = 3;
        List<Cell> cellsFirstRequest = generateMockCells("lookup(col-id,2)",1, true);
        List<CellResponseDto> expected = generateCellResponseDTO(cellsFirstRequest,  ExceptionMapper.MAX_LOOKUP_CHAIN_REACHED.getError());

        when(cellRepo.findAllByColumnId("col-id")).thenReturn(cellsFirstRequest);
        for (int i = 2; i< maxLookup +3; i++) { //start from 2 since rowIndex 1 is occupied by first cell  
            int idx = i + 1;
            Cell cell = generateMockCells("lookup(col-id," + idx + ")", i, true).getFirst();
            lenient().when(cellRepo.findByColumnIdAndRowIndex("col-id", i)).thenReturn(cell);
        }

        List<CellResponseDto> response = cellService.filterByColumnId("col-id");
        assertEquals(expected, response);
    }

    private List<Cell> generateMockCells(String value,int rowIndex, boolean isWithLookup) {
        List<Cell> cells = new ArrayList<>();
        Cell cell = new Cell("cell-id", value, "col-id", rowIndex, isWithLookup);
        cells.add(cell);
        return cells;
    }

    private List<CellResponseDto> generateCellResponseDTO(List<Cell> cells, String value) {
        List<CellResponseDto> expected = new ArrayList<>(); 
        Cell c = cells.getFirst();
        CellResponseDto colRes = new CellResponseDto(
            c.getId(),
            c.getRowIndex(),
            value,
            c.getColumnId(),
            c.getIsWithLookup(),
            null
        );
        expected.add(colRes);
        return expected;
     }
}