package com.rbc.holidays.enums;

public enum Country {
    USA("United States of America"),
    CANADA("Canada");

    private final String displayName;

    Country(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
