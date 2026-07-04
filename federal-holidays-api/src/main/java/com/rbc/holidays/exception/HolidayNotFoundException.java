package com.rbc.holidays.exception;

/**
 * Exception thrown when a requested holiday cannot be found.
 * 
 * <p>This exception is thrown when:</p>
 * <ul>
 *   <li>Retrieving a holiday by ID that doesn't exist</li>
 *   <li>Updating a holiday that has been deleted</li>
 *   <li>Deleting a holiday that doesn't exist</li>
 * </ul>
 * 
 * <p><strong>HTTP Mapping:</strong> Results in 404 NOT FOUND response</p>
 */
public class HolidayNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new holiday not found exception with the specified detail message.
     * 
     * @param message Detail message explaining what was not found
     */
    public HolidayNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new holiday not found exception for a specific holiday ID.
     * Automatically formats the message with the ID.
     * 
     * @param id Holiday ID that was not found
     */
    public HolidayNotFoundException(Long id) {
        super("Holiday not found with id: " + id);
    }
}
