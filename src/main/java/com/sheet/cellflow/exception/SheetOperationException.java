package com.sheet.cellflow.exception;

public class SheetOperationException extends RuntimeException {
    public SheetOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SheetOperationException(String message) {
        super(message);
    }
} 