package com.designtouch.backend.dto;

import com.designtouch.backend.entity.TeamMember;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id, String name, String role, String photoUrl,
        String linkedinUrl, String instagramUrl) {

    public static TeamMemberResponse from(TeamMember m) {
        return new TeamMemberResponse(
                m.getId(), m.getName(), m.getRole(), m.getPhotoUrl(),
                m.getLinkedinUrl(), m.getInstagramUrl());
    }
}
