package com.sheet.cellflow.sheet.controller;

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
import com.sheet.cellflow.exception.ExceptionMapper;
import com.sheet.cellflow.exception.SheetOperationException;
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
    public Optional<SheetResponseDto> getSheet(@PathVariable String sheetId) {
        return sheetService.find(sheetId);
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
   public ResponseEntity<CellResponseDto> setCell(@Valid @RequestBody CreateCellRequestDto request, @PathVariable String sheetId) {
        boolean isSheetExist = sheetService.isSheetExist(sheetId);
        if(!isSheetExist) {
            throw new IllegalArgumentException(ExceptionMapper.UNDEFINED_SHEET_ID.getError());
        }
        CellResponseDto result = sheetService.setCell(request, sheetId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}