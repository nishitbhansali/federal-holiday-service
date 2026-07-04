package com.rbc.holidays.exception;

public class HolidayNotFoundException extends RuntimeException {
    
    public HolidayNotFoundException(String message) {
        super(message);
    }
    
    public HolidayNotFoundException(Long id) {
        super("Holiday not found with id: " + id);
    }
}
