package com.designtouch.backend.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VideoAspectRatioConverter implements AttributeConverter<VideoAspectRatio, String> {

    @Override
    public String convertToDatabaseColumn(VideoAspectRatio attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public VideoAspectRatio convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return VideoAspectRatio.AUTO;
        }
        return VideoAspectRatio.fromString(dbData);
    }
}

