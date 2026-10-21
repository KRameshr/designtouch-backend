package com.designtouch.backend.service;

import com.designtouch.backend.dto.PortfolioCategoryRequest;
import com.designtouch.backend.dto.PortfolioCategoryResponse;
import com.designtouch.backend.entity.PortfolioCategory;
import com.designtouch.backend.entity.enums.CategoryType;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.PortfolioCategoryRepository;
import com.designtouch.backend.repository.PortfolioItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioCategoryService {

    private final PortfolioCategoryRepository categoryRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    @Transactional(readOnly = true)
    public List<PortfolioCategoryResponse> getCategories(CategoryType type, boolean includeAll) {
        List<PortfolioCategory> categories;
        if (type != null) {
            categories = includeAll
                    ? categoryRepository.findByCategoryTypeOrderByDisplayOrderAsc(type)
                    : categoryRepository.findByCategoryTypeAndIsActiveTrueOrderByDisplayOrderAsc(type);
        } else {
            categories = includeAll
                    ? categoryRepository.findAllByOrderByDisplayOrderAsc()
                    : categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        }

        return categories.stream()
                .map(cat -> {
                    long projectCount = portfolioItemRepository.countByCategoryRef_Id(cat.getId());
                    return PortfolioCategoryResponse.from(cat, projectCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioCategoryResponse getCategoryById(UUID id) {
        PortfolioCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        long projectCount = portfolioItemRepository.countByCategoryRef_Id(id);
        return PortfolioCategoryResponse.from(category, projectCount);
    }

    @Transactional
    public PortfolioCategoryResponse createCategory(PortfolioCategoryRequest request) {
        if (request.categoryType() == null) {
            throw new IllegalArgumentException("Category type is required (VIDEO_EDITING or THUMBNAIL_DESIGN).");
        }

        String name = request.name().trim();
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? slugify(request.slug())
                : slugify(name);

        if (categoryRepository.existsBySlugAndCategoryType(slug, request.categoryType())) {
            throw new IllegalArgumentException("A category with slug '" + slug + "' already exists for " + request.categoryType());
        }

        if (categoryRepository.existsByNameIgnoreCaseAndCategoryType(name, request.categoryType())) {
            throw new IllegalArgumentException("A category named '" + name + "' already exists for " + request.categoryType());
        }

        PortfolioCategory category = PortfolioCategory.builder()
                .name(name)
                .slug(slug)
                .categoryType(request.categoryType())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        PortfolioCategory saved = categoryRepository.save(category);
        return PortfolioCategoryResponse.from(saved, 0L);
    }

    @Transactional
    public PortfolioCategoryResponse updateCategory(UUID id, PortfolioCategoryRequest request) {
        PortfolioCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        // Requirement: Do NOT allow changing VIDEO_EDITING <-> THUMBNAIL_DESIGN
        if (request.categoryType() != null && request.categoryType() != category.getCategoryType()) {
            throw new IllegalArgumentException("Changing category type is not allowed. Category type is fixed after creation.");
        }

        String name = request.name().trim();
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? slugify(request.slug())
                : slugify(name);

        if (categoryRepository.existsBySlugAndCategoryTypeAndIdNot(slug, category.getCategoryType(), id)) {
            throw new IllegalArgumentException("A category with slug '" + slug + "' already exists for " + category.getCategoryType());
        }

        if (categoryRepository.existsByNameIgnoreCaseAndCategoryTypeAndIdNot(name, category.getCategoryType(), id)) {
            throw new IllegalArgumentException("A category named '" + name + "' already exists for " + category.getCategoryType());
        }

        category.setName(name);
        category.setSlug(slug);
        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }
        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }

        PortfolioCategory updated = categoryRepository.save(category);
        long projectCount = portfolioItemRepository.countByCategoryRef_Id(id);
        return PortfolioCategoryResponse.from(updated, projectCount);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        PortfolioCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        long count = portfolioItemRepository.countByCategoryRef_Id(id);
        if (count > 0) {
            throw new IllegalStateException(
                    "This category is currently used by " + count + " portfolio project" + (count == 1 ? "" : "s") + ". Reassign those projects before deleting it."
            );
        }

        categoryRepository.delete(category);
    }

    public static String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}

