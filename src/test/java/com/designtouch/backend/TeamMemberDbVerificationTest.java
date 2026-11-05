package com.designtouch.backend;

import com.designtouch.backend.entity.TeamMember;
import com.designtouch.backend.repository.TeamMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class TeamMemberDbVerificationTest {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Test
    void checkAndUpdateTeamMembersInDatabase() {
        List<TeamMember> members = teamMemberRepository.findAll();
        System.out.println("=== TEAM MEMBERS IN NEON DB (BEFORE) ===");
        for (TeamMember m : members) {
            System.out.println("MEMBER: id=" + m.getId()
                    + ", name=" + m.getName()
                    + ", role=" + m.getRole()
                    + ", linkedinUrl=" + m.getLinkedinUrl()
                    + ", instagramUrl=" + m.getInstagramUrl()
                    + ", isActive=" + m.getIsActive());
            if ("Rohit Sharma".equalsIgnoreCase(m.getName())) {
                m.setLinkedinUrl("https://www.linkedin.com/in/rohitsharma/");
                m.setInstagramUrl("https://www.instagram.com/rohitsharma/");
                teamMemberRepository.save(m);
            }
        }
        System.out.println("========================================");

        List<TeamMember> updatedMembers = teamMemberRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        System.out.println("=== ACTIVE TEAM MEMBERS IN NEON DB (AFTER) ===");
        for (TeamMember m : updatedMembers) {
            System.out.println("ACTIVE: name=" + m.getName()
                    + ", linkedinUrl=" + m.getLinkedinUrl()
                    + ", instagramUrl=" + m.getInstagramUrl());
            if ("Rohit Sharma".equalsIgnoreCase(m.getName())) {
                assertThat(m.getLinkedinUrl()).isEqualTo("https://www.linkedin.com/in/rohitsharma/");
                assertThat(m.getInstagramUrl()).isEqualTo("https://www.instagram.com/rohitsharma/");
            }
        }
        System.out.println("==============================================");
    }
}
