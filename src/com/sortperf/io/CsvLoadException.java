package com.sortperf.io;

/** Thrown when a file cannot be loaded as a valid CSV dataset. The message is user-friendly. */
public class CsvLoadException extends Exception {

    private static final long serialVersionUID = 1L;

    public CsvLoadException(String message) {
        super(message);
    }

    public CsvLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
