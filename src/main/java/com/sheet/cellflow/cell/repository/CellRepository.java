package com.sheet.cellflow.cell.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.sheet.cellflow.cell.model.Cell;

public interface CellRepository extends ListCrudRepository<Cell, String> {
    List<Cell> findAllByColumnId(String columnId);
    Cell findByColumnIdAndRowIndex(String columnId, int rowIndex);
}
