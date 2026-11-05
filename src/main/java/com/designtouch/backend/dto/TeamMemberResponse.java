package com.designtouch.backend.dto;

import com.designtouch.backend.entity.TeamMember;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        String name,
        String role,
        @JsonProperty("photoUrl") @JsonAlias("photo_url") String photoUrl,
        @JsonProperty("linkedinUrl") @JsonAlias("linkedin_url") String linkedinUrl,
        @JsonProperty("instagramUrl") @JsonAlias("instagram_url") String instagramUrl) {

    public static TeamMemberResponse from(TeamMember m) {
        return new TeamMemberResponse(
                m.getId(), m.getName(), m.getRole(), m.getPhotoUrl(),
                m.getLinkedinUrl(), m.getInstagramUrl());
    }
}
