package com.sheet.cellflow.sheet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sheet")
public class Sheet {
    @Id
    private String id;

    public Sheet() {}

    public Sheet(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
