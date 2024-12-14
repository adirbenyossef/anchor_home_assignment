package com.sheet.cellflow.lookup.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.dto.CreateCellRequestDto;
import com.sheet.cellflow.cell.model.Cell;
import com.sheet.cellflow.cell.model.SetCellRecord;
import com.sheet.cellflow.cell.service.CellService;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.column.service.ColumnService;
import com.sheet.cellflow.exception.CellOperationException;
import com.sheet.cellflow.exception.ExceptionMapper;
import com.sheet.cellflow.lookup.model.Lookup;

@Service
public class LookupService {
    @Value("${app.maxLookupChain}")
    private static final int MAX_LOOKUP_CHAIN = 2; 
    private final CellService cellService;
    private final ColumnService columnService;

    public LookupService (CellService cellService, ColumnService columnService){
        this.cellService = cellService;
        this.columnService = columnService;
    }

    public CellResponseDto setCell(CreateCellRequestDto req, String columnId, ColumnType columnType, String sheetId) {
          boolean isWithLookup = this.isWithLookup(req.value());
          if(!isWithLookup) {
            SetCellRecord partialCell = new SetCellRecord(columnId, req.rowIndex(), isWithLookup, req.value().toString(), columnType);
            return this.cellService.setCell(partialCell);
          } else {
            String cellValue = this.generateLookupCellValue(req, columnId, columnType, sheetId);
            SetCellRecord setCellInput = new SetCellRecord(columnId, req.rowIndex(), isWithLookup, cellValue,columnType);
            return this.cellService.setCell(setCellInput);
          }
    }

    private boolean isWithLookup(Object value) {
        try {
            Lookup lookup = new Lookup(value);
            return lookup.getColumnIdentifier() != null;
        } catch(Exception e) {
            return false;
        }
    }

    private String generateLookupCellValue(CreateCellRequestDto req, String columnId, ColumnType columnType, String sheetId) {
        String sourceValue = "lookup(" + columnId + "," + req.rowIndex() + ")";
        validateLookupFunction(req, columnType, sheetId, sourceValue);
        return sourceValue;
    }

    private void validateLookupFunction(CreateCellRequestDto req, ColumnType columnType, String sheetId, String sourceValue) {
        if(!columnType.isValidValue(req.value())) {
            throw new CellOperationException(ExceptionMapper.INVALID_LOOKUP_TARGET_CELL_IS_CURRENT_CELL.getError() + " value," + req.value() + " column type " + columnType);
        }
        boolean isLookupWithoutCycle = this.isLookupWithoutCycle(sourceValue, sheetId, columnType);
        if(!isLookupWithoutCycle) {
            throw new CellOperationException(ExceptionMapper.CYCLE_FAILED.getError()); 
        }
    }
    
    private boolean isLookupWithoutCycle(String sourceValue,String sheetId, ColumnType sourceType) {
        Set<String> visitedValues = new HashSet<>();
        visitedValues.add(sourceValue);
        Deque<String> stk = new ArrayDeque<>();
        stk.push(sourceValue);
        int i = 0;
        while(i< MAX_LOOKUP_CHAIN && !stk.isEmpty()) {
            String cur = stk.pop();
            if(visitedValues.contains(cur)) {
                throw new CellOperationException(ExceptionMapper.CYCLE_FAILED.getError());
            }
            visitedValues.add(cur);
            Lookup lookup = new Lookup(cur);
            Column targetColumn = this.columnService.findByNameAndSheetId(lookup.getColumnIdentifier(), sheetId);
            Cell targetCell = this.cellService.findByColumnIdAndRowIndex(lookup.getColumnIdentifier(), lookup.getRowIndex());
            if(!targetColumn.getColumnType().equals(sourceType)) {
                throw new CellOperationException(ExceptionMapper.INVALID_LOOKUP_TARGET_COLUMN_TYPE_NOT_MATCHED.getError() + " targetColumnType " + targetColumn.getColumnType() + " sourColumnType" + sourceType);
            }
            if(!isWithLookup(targetCell.getCellValue())) {
                return true;
            }
            stk.push(targetCell.getCellValue());
            i++;
        }
        return false;
    }
}
