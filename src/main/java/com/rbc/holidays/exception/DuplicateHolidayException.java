package com.rbc.holidays.exception;

/**
 * Exception thrown when attempting to create a holiday that already exists.
 * 
 * <p>This exception is thrown when:</p>
 * <ul>
 *   <li>Creating a new holiday for a country and date that already has a holiday</li>
 *   <li>Updating a holiday to a date that conflicts with an existing holiday</li>
 * </ul>
 * 
 * <p>The database enforces a unique constraint on (country, holiday_date),
 * and this exception provides business-level validation before database insertion.</p>
 * 
 * <p><strong>HTTP Mapping:</strong> Results in 409 CONFLICT response</p>
 */
public class DuplicateHolidayException extends RuntimeException {
    
    /**
     * Constructs a new duplicate holiday exception with the specified detail message.
     * 
     * @param message Detail message explaining the duplicate condition
     */
    public DuplicateHolidayException(String message) {
        super(message);
    }
}
