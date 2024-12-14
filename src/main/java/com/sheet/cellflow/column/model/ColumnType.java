package com.sheet.cellflow.column.model;

public enum ColumnType {
    STRING {
        @Override
        public boolean isValidValue(Object value) {
            return value == null || value instanceof String;
        }
    },
    INT {
        @Override
        public boolean isValidValue(Object value) {
            return value == null || value instanceof Integer;
        }
    },
    DOUBLE {
        @Override
        public boolean isValidValue(Object value) {
            return value == null || value instanceof Double;
        }
    },
    BOOLEAN {
        @Override
        public boolean isValidValue(Object value) {
            return value == null || value instanceof Boolean;
        }
    };
    public abstract boolean isValidValue(Object value);
}