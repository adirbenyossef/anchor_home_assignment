package com.sheet.cellflow.exception;

public class CellOperationException extends RuntimeException{
    
    public CellOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    public CellOperationException(String message) {
        super(message);
    }
}
