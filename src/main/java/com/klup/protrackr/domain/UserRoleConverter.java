package com.klup.protrackr.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.dbValue();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return UserRole.fromDbValue(dbData);
    }
}

