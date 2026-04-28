package com.klup.protrackr.domain;

import java.util.Locale;

public enum UserRole {
    STUDENT("student"),
    FACULTY("faculty"),
    ADMIN("admin");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static UserRole fromDbValue(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "student" -> STUDENT;
            case "faculty" -> FACULTY;
            case "admin" -> ADMIN;
            default -> throw new IllegalArgumentException("Unknown role: " + value);
        };
    }
}
