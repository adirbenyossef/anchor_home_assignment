package com.sheet.cellflow.config;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheet.cellflow.cell.model.Cell;
import com.sheet.cellflow.cell.repository.CellRepository;
import com.sheet.cellflow.column.model.Column;
import com.sheet.cellflow.column.repository.ColumnRepository;
import com.sheet.cellflow.sheet.model.Sheet;
import com.sheet.cellflow.sheet.repository.SheetRepository;

@Component
public class DataLoader implements CommandLineRunner  {
    
    private final SheetRepository sheetRepo;
    private final ColumnRepository columnRepo;
    private final CellRepository cellRepo;
    private final ObjectMapper objectMapper;

    public DataLoader( SheetRepository sheetRepo, ColumnRepository columnRepo, CellRepository cellRepo, ObjectMapper objectMapper) {
        this.sheetRepo = sheetRepo;
        this.columnRepo = columnRepo;
        this.cellRepo = cellRepo;
        this.objectMapper = objectMapper;
    }
    
    // init mock data
    @Override
    public void run(String... args) throws Exception {
        if(sheetRepo.count() == 0) {
            try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/sheet.json")) {
                sheetRepo.saveAll(objectMapper.readValue(inputStream,new TypeReference<List<Sheet>>(){}));
            }
        }
        if(columnRepo.count() == 0) {
            try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/column.json")) {
                columnRepo.saveAll(objectMapper.readValue(inputStream,new TypeReference<List<Column>>(){}));
            }
        }
        if(cellRepo.count() == 0) {
            try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/cell.json")) {
                cellRepo.saveAll(objectMapper.readValue(inputStream,new TypeReference<List<Cell>>(){}));
            }
        }
    }
}
