package com.rbc.holidays.enums;

/**
 * Enum representing supported countries for federal holidays.
 * 
 * <p>This enum defines the countries for which the system manages federal holidays.
 * Each country has a display name for user-facing messages.</p>
 * 
 * <p><strong>Supported Countries:</strong></p>
 * <ul>
 *   <li>USA - United States of America</li>
 *   <li>CANADA - Canada</li>
 * </ul>
 */
public enum Country {
    /** United States of America */
    USA("United States of America"),
    
    /** Canada */
    CANADA("Canada");

    private final String displayName;

    /**
     * Constructs a Country enum with the specified display name.
     * 
     * @param displayName Human-readable name of the country
     */
    Country(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name of the country.
     * 
     * @return Display name of the country
     */
    public String getDisplayName() {
        return displayName;
    }
}
