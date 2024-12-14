package com.sheet.cellflow.column.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.sheet.cellflow.column.model.Column;

public interface ColumnRepository extends ListCrudRepository<Column, String>  {
    List<Column> findByNameAndSheetId(String name, String sheetId);
    List<Column> findBySheetId(String sheetId);
}