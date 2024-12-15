package com.sheet.cellflow.sheet.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheet.cellflow.cell.dto.CellResponseDto;
import com.sheet.cellflow.cell.dto.CreateCellRequestDto;
import com.sheet.cellflow.exception.CellOperationException;
import com.sheet.cellflow.exception.ExceptionMapper;
import com.sheet.cellflow.exception.SheetOperationException;
import com.sheet.cellflow.sheet.dto.CellResponseApiDto;
import com.sheet.cellflow.sheet.dto.CreateSheetRequestDto;
import com.sheet.cellflow.sheet.dto.SheetResponseDto;
import com.sheet.cellflow.sheet.service.SheetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/sheet")
public class SheetController {
    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping("/{sheetId}")
    public ResponseEntity<SheetResponseDto> getSheet(@PathVariable String sheetId) {
        Optional<SheetResponseDto> sheetResponse = sheetService.find(sheetId);
        return sheetResponse
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<String> createSheet(@Valid @RequestBody CreateSheetRequestDto request) {
        try {
            String sheetId = sheetService.setSheet(request.columns());
            return ResponseEntity.status(HttpStatus.CREATED).body(sheetId);
        } catch (SheetOperationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{sheetId}/cell")
    public ResponseEntity<CellResponseApiDto> setCell(@Valid @RequestBody CreateCellRequestDto request, @PathVariable String sheetId) {
        if (!sheetService.isSheetExist(sheetId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CellResponseApiDto(null, ExceptionMapper.UNDEFINED_SHEET_ID.getError()));
        }
        try {
            CellResponseDto cellResponse = sheetService.setCell(request, sheetId);
            CellResponseApiDto response = new CellResponseApiDto(Collections.singletonList(cellResponse), null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CellOperationException e) {
            CellResponseApiDto errorResponse = new CellResponseApiDto(null, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}