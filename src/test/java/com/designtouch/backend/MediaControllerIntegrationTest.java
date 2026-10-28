package com.designtouch.backend;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class MediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @MockBean
    private Cloudinary cloudinary;

    private String adminToken;

    @BeforeEach
    void setUp() {
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseThrow();
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    @Test
    void testUploadVideo_unauthenticated_forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/media/upload/video").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUploadVideo_invalidFileType_badRequest() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "malicious.exe", "application/octet-stream", "evil bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/media/upload/video")
                        .file(textFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid video format. Allowed formats: MP4, WebM, MOV."));
    }

    @Test
    void testUploadVideo_emptyFile_badRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.mp4", "video/mp4", new byte[0]
        );

        mockMvc.perform(multipart("/api/media/upload/video")
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Video file is required and cannot be empty."));
    }

    @Test
    void testUploadVideo_authenticated_success() throws Exception {
        Uploader mockUploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(mockUploader);

        Map<String, Object> mockResult = Map.of(
                "secure_url", "https://res.cloudinary.com/test-cloud/video/upload/v12345/designtouch/videos/showcase.mp4",
                "public_id", "designtouch/videos/showcase",
                "resource_type", "video",
                "format", "mp4",
                "bytes", 204800L
        );
        when(mockUploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        MockMultipartFile validVideo = new MockMultipartFile(
                "file", "showcase.mp4", "video/mp4", new byte[]{1, 2, 3, 4, 5}
        );

        mockMvc.perform(multipart("/api/media/upload/video")
                        .file(validVideo)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Video uploaded successfully"))
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test-cloud/video/upload/v12345/designtouch/videos/showcase.mp4"))
                .andExpect(jsonPath("$.publicId").value("designtouch/videos/showcase"))
                .andExpect(jsonPath("$.resourceType").value("video"))
                .andExpect(jsonPath("$.format").value("mp4"))
                .andExpect(jsonPath("$.bytes").value(204800));
    }

    @Test
    void testUploadThumbnailImage_authenticated_success() throws Exception {
        Uploader mockUploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(mockUploader);

        Map<String, Object> mockResult = Map.of(
                "secure_url", "https://res.cloudinary.com/test-cloud/image/upload/v12345/designtouch/thumbnails/high_ctr.webp",
                "public_id", "designtouch/thumbnails/high_ctr",
                "resource_type", "image",
                "format", "webp",
                "bytes", 102400L
        );
        when(mockUploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        MockMultipartFile validImage = new MockMultipartFile(
                "file", "high_ctr.webp", "image/webp", new byte[]{1, 2, 3, 4, 5}
        );

        mockMvc.perform(multipart("/api/media/upload/thumbnail-image")
                        .file(validImage)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"))
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test-cloud/image/upload/v12345/designtouch/thumbnails/high_ctr.webp"))
                .andExpect(jsonPath("$.publicId").value("designtouch/thumbnails/high_ctr"))
                .andExpect(jsonPath("$.resourceType").value("image"))
                .andExpect(jsonPath("$.format").value("webp"))
                .andExpect(jsonPath("$.bytes").value(102400));
    }

    @Test
    void testUploadImage_invalidFileType_badRequest() throws Exception {
        MockMultipartFile badFile = new MockMultipartFile(
                "file", "script.sh", "application/x-sh", "echo hello".getBytes()
        );

        mockMvc.perform(multipart("/api/media/upload/image")
                        .file(badFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid image format. Allowed formats: JPEG, PNG, WebP, SVG."));
    }
}

