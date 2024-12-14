package com.sheet.cellflow.cell.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.model.Cell;
import com.sheet.cellflow.cell.model.SetCellRecord;
import com.sheet.cellflow.cell.repository.CellRepository;
import com.sheet.cellflow.exception.CellOperationException;
import com.sheet.cellflow.exception.ExceptionMapper;
import com.sheet.cellflow.lookup.model.Lookup;

@Service
public class CellService {
    @Value("${app.maxLookupChain}")
    private static final int MAX_LOOKUP_CHAIN = 2;
    private final CellRepository cellRepo;

    public CellService(CellRepository cellRepo) {
        this.cellRepo = cellRepo;
    }

    public List<CellResponseDto> filterByColumnId(String columnId) {
        List<Cell> cellEntities = this.cellRepo.findAllByColumnId(columnId);
        List<CellResponseDto> result = new ArrayList<>();
        for(Cell cellEntity: cellEntities) {
            CellResponseDto cell = generateCellResponseDto(cellEntity);
            result.add(cell);
        }
        return result;
    }

    public CellResponseDto setCell(SetCellRecord partialCell) {
        if(!partialCell.columnType().isValidValue(partialCell.cellValue())) {
            throw new CellOperationException(ExceptionMapper.INVALID_COLUMN_TYPE.getError() + " value, " + partialCell.cellValue() + "column type " + partialCell.columnType());
        }
        String cellId = UUID.randomUUID().toString();
        Cell cellEntity = new Cell()
            .setId(cellId)
            .setColumnId(partialCell.columnId())
            .setRowIndex(partialCell.rowIndex())
            .setCellValue(partialCell.cellValue())
            .setIsWithLookup(partialCell.isWithLookup());
        Cell entity = this.cellRepo.save(cellEntity);
        return generateCellResponseDto(entity);
    }

    public Cell findByColumnIdAndRowIndex(String columnId, int rowIndex) {
        return this.cellRepo.findByColumnIdAndRowIndex(columnId, rowIndex);
    }

    private String findLookupCellValue(String cellValue, int i) {
        if(i > MAX_LOOKUP_CHAIN) {
            return ExceptionMapper.MAX_LOOKUP_CHAIN_REACHED.getError();
        } else {
            Lookup lookup = new Lookup(cellValue);
            Cell cellEntity = this.findByColumnIdAndRowIndex(lookup.getColumnIdentifier(), lookup.getRowIndex());
            boolean isWithLookup = cellEntity.getIsWithLookup();
            if(isWithLookup) {
                return findLookupCellValue(cellEntity.getCellValue(), i + 1);
            }
            return cellEntity.getCellValue();
        }
    }

    private CellResponseDto generateCellResponseDto(Cell cellEntity) {
        Object value = Boolean.TRUE.equals(cellEntity.getIsWithLookup()) ? this.findLookupCellValue(cellEntity.getCellValue(), 0) : cellEntity.getCellValue();
        return new CellResponseDto(
            cellEntity.getId(), 
            cellEntity.getRowIndex(), 
            value,
            cellEntity.getColumnId(), 
            cellEntity.getIsWithLookup()
        );
    }
}
