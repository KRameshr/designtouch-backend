package com.designtouch.backend.dto;

import com.designtouch.backend.entity.ProcessStep;
import java.util.UUID;

public record ProcessStepResponse(UUID id, Short stepNumber, String title, String description, String icon) {

    public static ProcessStepResponse from(ProcessStep p) {
        return new ProcessStepResponse(p.getId(), p.getStepNumber(), p.getTitle(), p.getDescription(), p.getIcon());
    }
}
