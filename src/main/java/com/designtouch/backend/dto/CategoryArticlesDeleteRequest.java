package com.designtouch.backend.dto;

import java.util.List;
import java.util.UUID;

public record CategoryArticlesDeleteRequest(
        List<UUID> articleIds,
        Boolean deleteAll
) {
}

