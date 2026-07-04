package com.rbc.holidays.exception;

/**
 * Exception thrown when an invalid country code is provided.
 */
public class InvalidCountryException extends RuntimeException {
    
    /**
     * Constructs exception with country code and supported countries.
     * 
     * @param country Invalid country code
     * @param supportedCountries List of valid country codes
     */
    public InvalidCountryException(String country, String supportedCountries) {
        super(String.format("Invalid country '%s'. Supported countries: %s", country, supportedCountries));
    }
}
