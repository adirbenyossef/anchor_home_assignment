package com.sheet.cellflow.sheet.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.dto.CreateCellRequestDto;
import com.sheet.cellflow.column.dto.ColumnRequestDto;
import com.sheet.cellflow.column.dto.ColumnResponseDto;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.service.ColumnService;
import com.sheet.cellflow.exception.ExceptionMapper;
import com.sheet.cellflow.exception.SheetOperationException;
import com.sheet.cellflow.lookup.service.LookupService;
import com.sheet.cellflow.sheet.dto.SheetResponseDto;
import com.sheet.cellflow.sheet.model.Sheet;
import com.sheet.cellflow.sheet.repository.SheetRepository;

@Service
public class SheetService {

    private final SheetRepository sheetRepo;
    private final ColumnService columnService;
    private final LookupService lookupService;

    private final CircuitBreaker circuitBreaker;

    public SheetService(
        SheetRepository sheetRepo,
        ColumnService columnService,
        LookupService lookupService,
        CircuitBreaker circuitBreaker
    ) {
        this.sheetRepo = sheetRepo;
        this.columnService = columnService;
        this.lookupService = lookupService;
        this.circuitBreaker = circuitBreaker;
    }

    public Optional<SheetResponseDto> find(String sheetId) {
        if (!circuitBreaker.checkState()) {
            throw new SheetOperationException("Circuit breaker is OPEN. Requests are blocked.");
        }
        try {
            if(!sheetRepo.existsById(sheetId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ExceptionMapper.UNDEFINED_SHEET_ID.getError());
            }
            List<ColumnResponseDto> columns = columnService.filterBySheetId(sheetId);
            circuitBreaker.reset(); // Success, reset the circuit breaker
            return Optional.of(new SheetResponseDto(sheetId, columns));
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    public String setSheet(List<ColumnRequestDto> columns) {
        if (!circuitBreaker.checkState()) {
            throw new SheetOperationException("Circuit breaker is OPEN. Requests are blocked.");
        }
        try {
            Sheet sheetEntity = new Sheet();
            String sheetId = UUID.randomUUID().toString();
            sheetEntity.setId(sheetId);
            this.sheetRepo.save(sheetEntity);
            this.columnService.setColumns(columns, sheetId);
            circuitBreaker.reset(); // Success, reset the circuit breaker
            return sheetId;
        } catch(Exception e) {
            circuitBreaker.recordFailure();
            throw new SheetOperationException(
                    ExceptionMapper.SHEET_CREATE_FAILED.getError() + e.getMessage(), e);
        }
    }

    public boolean isSheetExist(String sheetId) {
        if (!circuitBreaker.checkState()) {
            throw new SheetOperationException("Circuit breaker is OPEN. Requests are blocked.");
        }
        try {
            boolean exists = sheetRepo.existsById(sheetId);
            circuitBreaker.reset(); // Success, reset the circuit breaker
            return exists;
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    public CellResponseDto setCell(CreateCellRequestDto req, String sheetId) {
        if (!circuitBreaker.checkState()) {
            throw new SheetOperationException("Circuit breaker is OPEN. Requests are blocked.");
        }
        try {
            Column column = columnService.findByNameAndSheetId(req.columnName(), sheetId);
            CellResponseDto response = lookupService.setCell(
                    req, column.getId(), column.getColumnType(), sheetId);
            circuitBreaker.reset(); // Success, reset the circuit breaker
            return response;
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }
}
