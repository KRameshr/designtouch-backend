package com.designtouch.backend.dto;

import jakarta.validation.constraints.Size;

public record ContactPageContentRequest(
        @Size(max = 150, message = "Hero eyebrow must not exceed 150 characters")
        String heroEyebrow,

        @Size(max = 150, message = "Headline line 1 must not exceed 150 characters")
        String heroHeadlineLine1,

        @Size(max = 150, message = "Headline highlight must not exceed 150 characters")
        String heroHeadlineHighlight,

        String heroDescription,

        @Size(max = 100, message = "Badge 1 title must not exceed 100 characters")
        String badge1Title,

        @Size(max = 150, message = "Badge 1 subtitle must not exceed 150 characters")
        String badge1Subtitle,

        @Size(max = 100, message = "Badge 2 title must not exceed 100 characters")
        String badge2Title,

        @Size(max = 150, message = "Badge 2 subtitle must not exceed 150 characters")
        String badge2Subtitle,

        String heroBannerImageUrl,
        String heroBannerImagePublicId,

        @Size(max = 255, message = "Alt text must not exceed 255 characters")
        String heroBannerImageAlt,

        @Size(max = 50, message = "Object position must not exceed 50 characters")
        String heroBannerObjectPosition,

        @Size(max = 150, message = "Direct contact eyebrow must not exceed 150 characters")
        String directContactEyebrow,

        @Size(max = 150, message = "Direct contact heading must not exceed 150 characters")
        String directContactHeading,

        String directContactDescription,

        @Size(max = 100, message = "Direct contact email label must not exceed 100 characters")
        String directContactEmailLabel,

        @Size(max = 255, message = "Direct contact email must not exceed 255 characters")
        String directContactEmail,

        @Size(max = 150, message = "Direct contact email subtitle must not exceed 150 characters")
        String directContactEmailSub,

        @Size(max = 100, message = "Direct contact phone label must not exceed 100 characters")
        String directContactPhoneLabel,

        @Size(max = 100, message = "Direct contact phone must not exceed 100 characters")
        String directContactPhone,

        @Size(max = 150, message = "Direct contact phone subtitle must not exceed 150 characters")
        String directContactPhoneSub,

        @Size(max = 100, message = "Direct contact location label must not exceed 100 characters")
        String directContactLocationLabel,

        @Size(max = 255, message = "Direct contact location must not exceed 255 characters")
        String directContactLocation,

        @Size(max = 150, message = "Direct contact location subtitle must not exceed 150 characters")
        String directContactLocationSub,

        @Size(max = 100, message = "Direct contact working hours label must not exceed 100 characters")
        String directContactWorkingHoursLabel,

        @Size(max = 255, message = "Direct contact working hours must not exceed 255 characters")
        String directContactWorkingHours,

        @Size(max = 150, message = "Direct contact working hours subtitle must not exceed 150 characters")
        String directContactWorkingHoursSub,

        Boolean directContactEmailEnabled,
        Boolean directContactPhoneEnabled,
        Boolean directContactLocationEnabled,
        Boolean directContactWorkingHoursEnabled,
        String socialLinks
) {
    public ContactPageContentRequest(
            String heroEyebrow,
            String heroHeadlineLine1,
            String heroHeadlineHighlight,
            String heroDescription,
            String badge1Title,
            String badge1Subtitle,
            String badge2Title,
            String badge2Subtitle,
            String heroBannerImageUrl,
            String heroBannerImagePublicId,
            String heroBannerImageAlt,
            String heroBannerObjectPosition,
            String directContactEyebrow,
            String directContactHeading,
            String directContactDescription,
            String directContactEmailLabel,
            String directContactEmail,
            String directContactEmailSub,
            String directContactPhoneLabel,
            String directContactPhone,
            String directContactPhoneSub,
            String directContactLocationLabel,
            String directContactLocation,
            String directContactLocationSub,
            String directContactWorkingHoursLabel,
            String directContactWorkingHours,
            String directContactWorkingHoursSub
    ) {
        this(heroEyebrow, heroHeadlineLine1, heroHeadlineHighlight, heroDescription,
                badge1Title, badge1Subtitle, badge2Title, badge2Subtitle,
                heroBannerImageUrl, heroBannerImagePublicId, heroBannerImageAlt, heroBannerObjectPosition,
                directContactEyebrow, directContactHeading, directContactDescription,
                directContactEmailLabel, directContactEmail, directContactEmailSub,
                directContactPhoneLabel, directContactPhone, directContactPhoneSub,
                directContactLocationLabel, directContactLocation, directContactLocationSub,
                directContactWorkingHoursLabel, directContactWorkingHours, directContactWorkingHoursSub,
                true, true, true, true, null);
    }
}

