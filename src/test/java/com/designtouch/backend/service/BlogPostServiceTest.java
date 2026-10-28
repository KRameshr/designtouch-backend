package com.designtouch.backend.service;

import com.designtouch.backend.dto.BlogPostDetailResponse;
import com.designtouch.backend.dto.BlogPostRequest;
import com.designtouch.backend.entity.BlogCategory;
import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogPostStatus;
import com.designtouch.backend.repository.BlogCategoryRepository;
import com.designtouch.backend.repository.BlogPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlogPostServiceTest {

    private final Map<UUID, BlogPost> posts = new HashMap<>();
    private final Map<UUID, BlogCategory> categories = new HashMap<>();

    private BlogPostRepository repository;
    private BlogCategoryRepository blogCategoryRepository;
    private MediaService mediaService;
    private BlogPostService service;

    @BeforeEach
    void setUp() {
        posts.clear();
        categories.clear();

        repository = (BlogPostRepository) Proxy.newProxyInstance(
                BlogPostRepository.class.getClassLoader(),
                new Class<?>[]{BlogPostRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("existsBySlugIgnoreCase".equals(name)) {
                        String s = (String) args[0];
                        return posts.values().stream()
                                .anyMatch(p -> p.getSlug() != null && p.getSlug().equalsIgnoreCase(s));
                    }
                    if ("existsBySlugIgnoreCaseAndIdNot".equals(name)) {
                        String s = (String) args[0];
                        UUID excludeId = (UUID) args[1];
                        return posts.values().stream()
                                .anyMatch(p -> p.getSlug() != null && p.getSlug().equalsIgnoreCase(s) && !excludeId.equals(p.getId()));
                    }
                    if ("save".equals(name)) {
                        BlogPost p = (BlogPost) args[0];
                        if (p.getId() == null) {
                            p.setId(UUID.randomUUID());
                        }
                        posts.put(p.getId(), p);
                        return p;
                    }
                    if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return Optional.ofNullable(posts.get(id));
                    }
                    if ("countByCoverImagePublicIdAndIdNot".equals(name)) {
                        String pubId = (String) args[0];
                        UUID excludeId = (UUID) args[1];
                        return posts.values().stream()
                                .filter(p -> pubId.equals(p.getCoverImagePublicId()) && !excludeId.equals(p.getId()))
                                .count();
                    }
                    if ("delete".equals(name)) {
                        BlogPost p = (BlogPost) args[0];
                        posts.remove(p.getId());
                        return null;
                    }
                    if ("toString".equals(name)) {
                        return "FakeBlogPostRepository";
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                    return null;
                }
        );

        blogCategoryRepository = (BlogCategoryRepository) Proxy.newProxyInstance(
                BlogCategoryRepository.class.getClassLoader(),
                new Class<?>[]{BlogCategoryRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return Optional.ofNullable(categories.get(id));
                    }
                    if ("findBySlugIgnoreCase".equals(name)) {
                        String slug = (String) args[0];
                        return categories.values().stream()
                                .filter(c -> c.getSlug() != null && c.getSlug().equalsIgnoreCase(slug))
                                .findFirst();
                    }
                    if ("findByNameIgnoreCase".equals(name)) {
                        String catName = (String) args[0];
                        return categories.values().stream()
                                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(catName))
                                .findFirst();
                    }
                    if ("toString".equals(name)) {
                        return "FakeBlogCategoryRepository";
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                    return null;
                }
        );

        mediaService = new MediaService(null) {
            @Override
            public void deleteImage(String publicId) {
                // fake no-op
            }
        };

        service = new BlogPostService(repository, blogCategoryRepository, mediaService);
    }

    @Test
    @DisplayName("Create article: successful creation with all fields, SEO metadata and published status")
    void testCreate_success_publishedArticle() {
        UUID categoryId = UUID.randomUUID();
        BlogCategory category = BlogCategory.builder()
                .id(categoryId)
                .name("Video Editing")
                .slug("VIDEO_EDITING")
                .displayOrder(1)
                .build();
        categories.put(categoryId, category);

        BlogPostRequest request = new BlogPostRequest(
                "Mastering Video Editing",
                "mastering-video-editing",
                "Complete guide to cinematic editing.",
                "# Comprehensive Guide\n\nTips on video editing.",
                "https://res.cloudinary.com/demo/image/upload/cover.jpg",
                "designtouch/blog/covers/cover123",
                "VIDEO_EDITING",
                categoryId,
                5,
                BlogPostStatus.PUBLISHED,
                null,
                null,
                true,
                "Mastering Video Editing | DesignTouch+",
                "Learn cinematic video editing step by step.",
                List.of("video editing", "premiere pro"),
                List.of("video editing guide", "tutorial")
        );

        BlogPostDetailResponse response = service.create(request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Mastering Video Editing");
        assertThat(response.slug()).isEqualTo("mastering-video-editing");
        assertThat(response.status()).isEqualTo(BlogPostStatus.PUBLISHED);
        assertThat(response.isPublished()).isTrue();
        assertThat(response.category()).isEqualTo("VIDEO_EDITING");
        assertThat(response.categoryId()).isEqualTo(categoryId);
        assertThat(response.seoTitle()).isEqualTo("Mastering Video Editing | DesignTouch+");
        assertThat(response.metaDescription()).isEqualTo("Learn cinematic video editing step by step.");
        assertThat(response.tags()).containsExactly("video editing", "premiere pro");
        assertThat(response.focusKeywords()).containsExactly("video editing guide", "tutorial");
        assertThat(response.viewCount()).isEqualTo(0);
        assertThat(response.publishedAt()).isNotNull();

        assertThat(posts).hasSize(1);
        BlogPost saved = posts.values().iterator().next();
        assertThat(saved.getTitle()).isEqualTo("Mastering Video Editing");
        assertThat(saved.getSlug()).isEqualTo("mastering-video-editing");
        assertThat(saved.getCategoryRef()).isEqualTo(category);
    }

    @Test
    @DisplayName("Create article: resolves category by slug when categoryId is omitted")
    void testCreate_resolvesCategoryBySlug() {
        UUID categoryId = UUID.randomUUID();
        BlogCategory category = BlogCategory.builder()
                .id(categoryId)
                .name("Motion Graphics")
                .slug("MOTION_GRAPHICS")
                .build();
        categories.put(categoryId, category);

        BlogPostRequest request = new BlogPostRequest(
                "Motion Graphics Guide",
                "motion-graphics-guide",
                "Excerpt here",
                "Body content",
                null,
                null,
                "MOTION_GRAPHICS",
                null,
                3,
                BlogPostStatus.PUBLISHED,
                null,
                null,
                true,
                null,
                null,
                List.of(),
                List.of()
        );

        BlogPostDetailResponse response = service.create(request);

        assertThat(response.category()).isEqualTo("MOTION_GRAPHICS");
        assertThat(response.categoryId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("Create article: rejects duplicate slug with clear IllegalArgumentException")
    void testCreate_duplicateSlug_throwsIllegalArgumentException() {
        // Prepopulate an existing post with slug "10-video-editing-tips"
        BlogPost existing = BlogPost.builder()
                .id(UUID.randomUUID())
                .title("Existing Tips")
                .slug("10-video-editing-tips")
                .content("Content")
                .status(BlogPostStatus.PUBLISHED)
                .isPublished(true)
                .build();
        posts.put(existing.getId(), existing);

        BlogPostRequest request = new BlogPostRequest(
                "10 Video Editing Tips",
                "10-video-editing-tips",
                "Excerpt",
                "Content",
                null,
                null,
                "VIDEO_EDITING",
                null,
                5,
                BlogPostStatus.PUBLISHED,
                null,
                null,
                true,
                null,
                null,
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        // Ensure no second post was added
        assertThat(posts).hasSize(1);
    }

    @Test
    @DisplayName("Create article: rejects blank or empty slug")
    void testCreate_blankSlug_throwsIllegalArgumentException() {
        BlogPostRequest request = new BlogPostRequest(
                "Article With No Slug",
                "   ",
                "Excerpt",
                "Content",
                null,
                null,
                "VIDEO_EDITING",
                null,
                5,
                BlogPostStatus.PUBLISHED,
                null,
                null,
                true,
                null,
                null,
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Article URL slug cannot be empty");

        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("Create article: scheduled post sets isPublished=false and scheduledFor date")
    void testCreate_scheduledPost_success() {
        OffsetDateTime future = OffsetDateTime.now().plusDays(2);

        BlogPostRequest request = new BlogPostRequest(
                "Scheduled Post",
                "scheduled-post",
                "Excerpt",
                "Content",
                null,
                null,
                "VIDEO_EDITING",
                null,
                4,
                BlogPostStatus.SCHEDULED,
                future,
                null,
                false,
                null,
                null,
                List.of(),
                List.of()
        );

        BlogPostDetailResponse response = service.create(request);

        assertThat(response.status()).isEqualTo(BlogPostStatus.SCHEDULED);
        assertThat(response.isPublished()).isFalse();
        assertThat(response.scheduledFor()).isEqualTo(future);
    }

    @Test
    @DisplayName("Update article: rejects duplicate slug on another article")
    void testUpdate_duplicateSlug_throwsIllegalArgumentException() {
        UUID post1Id = UUID.randomUUID();
        BlogPost post1 = BlogPost.builder()
                .id(post1Id)
                .title("Post 1")
                .slug("first-post")
                .content("Content 1")
                .status(BlogPostStatus.PUBLISHED)
                .build();
        posts.put(post1Id, post1);

        UUID post2Id = UUID.randomUUID();
        BlogPost post2 = BlogPost.builder()
                .id(post2Id)
                .title("Post 2")
                .slug("second-post")
                .content("Content 2")
                .status(BlogPostStatus.PUBLISHED)
                .build();
        posts.put(post2Id, post2);

        // Try updating post2 to use post1's slug
        BlogPostRequest request = new BlogPostRequest(
                "Updated Post 2",
                "first-post",
                "Excerpt",
                "Updated Content",
                null,
                null,
                "VIDEO_EDITING",
                null,
                5,
                BlogPostStatus.PUBLISHED,
                null,
                null,
                true,
                null,
                null,
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> service.update(post2Id, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Update article: successfully updates content, title, and SEO fields")
    void testUpdate_success() {
        UUID postId = UUID.randomUUID();
        BlogPost existingPost = BlogPost.builder()
                .id(postId)
                .title("Original Title")
                .slug("original-slug")
                .content("Original content")
                .status(BlogPostStatus.DRAFT)
                .isPublished(false)
                .build();
        posts.put(postId, existingPost);

        BlogPostRequest request = new BlogPostRequest(
                "Updated Title",
                "updated-slug",
                "Updated excerpt",
                "Updated content with rich text",
                "https://res.cloudinary.com/demo/image/upload/new-cover.jpg",
                "designtouch/blog/covers/new123",
                "VIDEO_EDITING",
                null,
                7,
                BlogPostStatus.PUBLISHED,
                null,
                null,
                true,
                "New SEO Title",
                "New Meta Description",
                List.of("tag1", "tag2"),
                List.of("kw1")
        );

        BlogPostDetailResponse response = service.update(postId, request);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.slug()).isEqualTo("updated-slug");
        assertThat(response.status()).isEqualTo(BlogPostStatus.PUBLISHED);
        assertThat(response.isPublished()).isTrue();
        assertThat(response.seoTitle()).isEqualTo("New SEO Title");
        assertThat(response.metaDescription()).isEqualTo("New Meta Description");
        assertThat(response.tags()).containsExactly("tag1", "tag2");
        assertThat(response.focusKeywords()).containsExactly("kw1");
    }
}

