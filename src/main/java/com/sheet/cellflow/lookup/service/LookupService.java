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
        boolean hasLookup = isWithLookup(req.value());
        String cellValue;

        if (hasLookup) {
            String sourceValue = generateLookupValue(columnId, req.rowIndex());
            Lookup lookup = getLookupValue(sourceValue, req, sheetId, columnType);
            cellValue = generateLookupValue(lookup.getColumnIdentifier(), lookup.getRowIndex());
        } else {
            cellValue = req.value().toString();
        }

        SetCellRecord partialCell = new SetCellRecord(columnId, req.rowIndex(), hasLookup, cellValue, columnType);
        return cellService.setCell(partialCell);
    }

    private String generateLookupValue(String columnId, int rowIndex) {
        return "lookup(" + columnId + "," + rowIndex+ ")";
    }

    private boolean isWithLookup(Object value) {
        try {
            Lookup lookup = new Lookup(value);
            return lookup.getColumnIdentifier() != null;
        } catch(Exception e) {
            return false;
        }
    }

      private Lookup getLookupValue(String sourceValue, CreateCellRequestDto request, String sheetId, ColumnType sourceType) {
        Set<String> visitedValues = new HashSet<>();
        visitedValues.add(sourceValue);

        Lookup targetLookup = new Lookup(request.value());
        Deque<String> stack = new ArrayDeque<>();
        Column targetLookupColumn = columnService.findByNameAndSheetId(targetLookup.getColumnIdentifier(), sheetId);
        String targetValue = generateLookupValue(targetLookupColumn.getId(), targetLookup.getRowIndex());
        stack.push(targetValue);

        int lookupCount = 0;
        while (lookupCount < MAX_LOOKUP_CHAIN && !stack.isEmpty()) {
            String currentValue = stack.pop();

            if (lookupCount != 0 && visitedValues.contains(currentValue)) {
                throw new CellOperationException(ExceptionMapper.CYCLE_FAILED.getError());
            }

            visitedValues.add(currentValue);
            Lookup lookup = new Lookup(currentValue);
            Column targetColumn = columnService.findById(lookup.getColumnIdentifier());
            Cell targetCell = cellService.findByColumnIdAndRowIndex(lookup.getColumnIdentifier(), lookup.getRowIndex());

            if (!targetColumn.getColumnType().equals(sourceType)) {
                throw new CellOperationException(ExceptionMapper.INVALID_COLUMN_TYPE.getError() +
                        " targetColumnType: " + targetColumn.getColumnType() +
                        " sourceColumnType: " + sourceType);
            }

            if (!isWithLookup(targetCell.getCellValue())) {
                lookup.setValue(targetCell.getCellValue());
                lookup.setColumnType(targetColumn.getColumnType());
                return lookup;
            }

            stack.push(targetCell.getCellValue());
            lookupCount++;
        }

        throw new CellOperationException(ExceptionMapper.MAX_LOOKUP_CHAIN_REACHED.getError());
    }
}
