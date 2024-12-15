package com.sheet.cellflow.column.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.service.CellService;
import com.sheet.cellflow.column.dto.ColumnRequestDto;
import com.sheet.cellflow.column.dto.ColumnResponseDto;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.model.ColumnType;
import com.sheet.cellflow.column.repository.ColumnRepository;
import com.sheet.cellflow.exception.CellOperationException;
import com.sheet.cellflow.exception.ExceptionMapper;
import com.sheet.cellflow.exception.SheetOperationException;


@Service
public class ColumnService {
    
    private final ColumnRepository columnRepo;
    private final CellService cellService;
    
    public ColumnService(ColumnRepository columnRepo, CellService cellService) {
        this.columnRepo = columnRepo;
        this.cellService = cellService;
    }

    public List<ColumnResponseDto> filterBySheetId(String sheetId) {
        List<ColumnResponseDto> result = new ArrayList<>();
        List<Column> columnsEntities = columnRepo.findBySheetId(sheetId);
        for (Column column : columnsEntities) {
            List<CellResponseDto> cells = cellService.filterByColumnId(column.getId());
            ColumnType columnType = column.getColumnType();
            ColumnResponseDto columnDto = new ColumnResponseDto(
                column.getId(),
                column.getName(), 
                columnType,
                column.getSheetId(), 
                cells
            );
            result.add(columnDto);
        }
        return result;
    }

    public List<Column> setColumns(List<ColumnRequestDto> columns, String sheetId) {
        try {
            List<Column> result = new ArrayList<>();
            for(ColumnRequestDto col: columns) {
                String columnId = UUID.randomUUID().toString();
                String typeStr = col.type().toUpperCase();
                if (!isValidColumnType(typeStr)) {
                    throw new SheetOperationException(ExceptionMapper.INVALID_COLUMN_TYPE.getError() + col.type());
                }
                ColumnType type = ColumnType.valueOf(typeStr);
                Column columnEntity = new Column(columnId, sheetId, col.name(), type);
                result.add(columnEntity);
                this.columnRepo.save(columnEntity);
            }
            return result;
        } catch(Exception e) {
            throw new SheetOperationException(ExceptionMapper.SAVE_COLUMN_FAILED.getError() + e.getMessage(), e);
        }
    }

    public Column findById(String id) {
        return this.columnRepo.findById(id).orElseThrow(() -> new CellOperationException("column not found"));
    }

    public Column findByNameAndSheetId(String columnName, String sheetId) {
        List<Column> cols = this.columnRepo.findByNameAndSheetId(columnName, sheetId);
         if(cols.size() > 1) {
            throw new CellOperationException(ExceptionMapper.DUPLICATE_COLUMN_NAME.getError());
        }
        return cols.getFirst();
    }

    private boolean isValidColumnType(String type) {
        return type.equals("STRING") || 
               type.equals("INT") || 
               type.equals("DOUBLE") || 
               type.equals("BOOLEAN");
    }
}
