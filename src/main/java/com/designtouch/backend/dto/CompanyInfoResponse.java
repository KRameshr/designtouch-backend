package com.designtouch.backend.dto;

import com.designtouch.backend.entity.CompanyInfo;

// Reused site-wide: Footer (every page) + Contact page "GET IN TOUCH" cards
public record CompanyInfoResponse(
        String email, String phone, String whatsappNumber, String address,
        String workingHours, String mapEmbedUrl) {

    public static CompanyInfoResponse from(CompanyInfo c) {
        return new CompanyInfoResponse(
                c.getEmail(), c.getPhone(), c.getWhatsappNumber(), c.getAddress(),
                c.getWorkingHours(), c.getMapEmbedUrl());
    }
}
