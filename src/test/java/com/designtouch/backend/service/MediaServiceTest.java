package com.designtouch.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.designtouch.backend.dto.MediaUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(cloudinary);
        ReflectionTestUtils.setField(mediaService, "maxVideoSizeBytes", 104857600L); // 100MB
        ReflectionTestUtils.setField(mediaService, "videoFolder", "designtouch/videos");
    }

    @Test
    void testUploadVideo_emptyFile_throwsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", new byte[0]
        );

        assertThatThrownBy(() -> mediaService.uploadVideo(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void testUploadVideo_nullFile_throwsException() {
        assertThatThrownBy(() -> mediaService.uploadVideo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");
    }

    @Test
    void testUploadVideo_invalidFileType_throwsException() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "hello world".getBytes()
        );

        assertThatThrownBy(() -> mediaService.uploadVideo(textFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid video format");
    }

    @Test
    void testUploadVideo_fileTooLarge_throwsException() {
        ReflectionTestUtils.setField(mediaService, "maxVideoSizeBytes", 100L); // 100 bytes limit for test

        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.mp4", "video/mp4", new byte[200]
        );

        assertThatThrownBy(() -> mediaService.uploadVideo(largeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds the maximum allowed limit");
    }

    @Test
    void testUploadVideo_validFile_success() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> mockResult = Map.of(
                "secure_url", "https://res.cloudinary.com/demo/video/upload/v1/designtouch/videos/reel.mp4",
                "public_id", "designtouch/videos/reel",
                "resource_type", "video",
                "format", "mp4",
                "bytes", 1024L
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        MockMultipartFile validFile = new MockMultipartFile(
                "file", "reel.mp4", "video/mp4", new byte[]{1, 2, 3, 4}
        );

        MediaUploadResponse response = mediaService.uploadVideo(validFile);

        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.url()).isEqualTo("https://res.cloudinary.com/demo/video/upload/v1/designtouch/videos/reel.mp4");
        assertThat(response.publicId()).isEqualTo("designtouch/videos/reel");
        assertThat(response.resourceType()).isEqualTo("video");
        assertThat(response.format()).isEqualTo("mp4");
        assertThat(response.bytes()).isEqualTo(1024L);

        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testUploadPortfolioVideo_success() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> mockResult = Map.of(
                "secure_url", "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio/clip.mp4",
                "public_id", "designtouch/portfolio/clip",
                "resource_type", "video",
                "format", "mp4",
                "bytes", 2048L
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        MockMultipartFile validFile = new MockMultipartFile(
                "file", "clip.mp4", "video/mp4", new byte[]{5, 6, 7, 8}
        );

        MediaUploadResponse response = mediaService.uploadPortfolioVideo(validFile);

        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo("https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio/clip.mp4");
        assertThat(response.publicId()).isEqualTo("designtouch/portfolio/clip");

        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testUploadPortfolioIntroVideo_success() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> mockResult = Map.of(
                "secure_url", "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio-intro/reel.mp4",
                "public_id", "designtouch/portfolio-intro/reel",
                "resource_type", "video",
                "format", "mp4",
                "bytes", 4096L
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        MockMultipartFile validFile = new MockMultipartFile(
                "file", "reel.mp4", "video/mp4", new byte[]{9, 10, 11, 12}
        );

        MediaUploadResponse response = mediaService.uploadPortfolioIntroVideo(validFile);

        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo("https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio-intro/reel.mp4");
        assertThat(response.publicId()).isEqualTo("designtouch/portfolio-intro/reel");

        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testDeleteVideo_callsDestroy() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(), anyMap())).thenReturn(Map.of("result", "ok"));

        mediaService.deleteVideo("designtouch/portfolio/clip");

        verify(uploader).destroy(any(), anyMap());
    }
}

