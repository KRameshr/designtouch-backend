package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Tool;
import java.util.UUID;

public record ToolResponse(UUID id, String name, String iconKey) {

    public static ToolResponse from(Tool t) {
        return new ToolResponse(t.getId(), t.getName(), t.getIconKey());
    }
}
