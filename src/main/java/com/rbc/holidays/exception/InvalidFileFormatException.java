package com.rbc.holidays.exception;

/**
 * Exception thrown when uploaded file format is invalid or cannot be parsed.
 * 
 * <p>This exception is thrown when:</p>
 * <ul>
 *   <li>File is empty</li>
 *   <li>File content type is not supported (only CSV and JSON allowed)</li>
 *   <li>File structure doesn't match expected format</li>
 *   <li>Parsing fails due to malformed data</li>
 * </ul>
 * 
 * <p><strong>Supported Formats:</strong></p>
 * <ul>
 *   <li>CSV - text/csv with headers: holidayName, holidayDate, country, isRecurring, description</li>
 *   <li>JSON - application/json with array of holiday objects</li>
 * </ul>
 * 
 * <p><strong>HTTP Mapping:</strong> Results in 400 BAD REQUEST response</p>
 */
public class InvalidFileFormatException extends RuntimeException {
    
    /**
     * Constructs a new invalid file format exception with the specified detail message.
     * 
     * @param message Detail message explaining the format issue
     */
    public InvalidFileFormatException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new invalid file format exception with message and cause.
     * Used when wrapping parsing exceptions.
     * 
     * @param message Detail message explaining the format issue
     * @param cause The underlying cause (parsing exception)
     */
    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
