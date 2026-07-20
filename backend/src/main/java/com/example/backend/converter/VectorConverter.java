package com.example.backend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VectorConverter implements AttributeConverter<float[], Object> {

    @Override
    public Object convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) return null;
        return attribute; // Hibernate 6 + pgvector driver gèrent l'objet brut
    }

    @Override
    public float[] convertToEntityAttribute(Object dbData) {
        if (dbData == null) return null;
        return (float[]) dbData;
    }
}