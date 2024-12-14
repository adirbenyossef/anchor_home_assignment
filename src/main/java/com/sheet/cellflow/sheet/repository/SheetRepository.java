package com.sheet.cellflow.sheet.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.sheet.cellflow.sheet.model.Sheet;

public interface SheetRepository extends ListCrudRepository<Sheet, String> {}