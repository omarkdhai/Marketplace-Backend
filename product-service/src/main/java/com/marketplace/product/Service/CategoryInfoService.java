package com.marketplace.product.Service;

import com.marketplace.product.Entity.CategoryInfo;
import com.marketplace.product.Repository.CategoryInfoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoryInfoService {
    @Inject
    CategoryInfoRepository categoryInfoRepository;

    // Add CategoryInfo
    public CategoryInfo addCategoryInfo(CategoryInfo categoryInfo) {
        try {
            if (categoryInfo != null) {
                categoryInfo.persist();
                return categoryInfo;
            } else {
                throw new IllegalArgumentException("CategoryInfo cannot be null");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while saving the category", e);
        }
    }

    // Get all CategoryInfos
    public List<CategoryInfo> getAllCategoryInfos() {
        return categoryInfoRepository.listAll();
    }

    // Get CategoryInfo by ID
    public Optional<CategoryInfo> getCategoryInfoById(String id) {
        return CategoryInfo.findByIdOptional(new ObjectId(id));
    }

    // Update CategoryInfo
    public CategoryInfo updateCategoryInfo(String id, CategoryInfo categoryInfo) {
        CategoryInfo existingCategory = CategoryInfo.findById(new ObjectId(id));
        if (existingCategory != null) {
            existingCategory.setName(categoryInfo.getName());
            existingCategory.setDescription(categoryInfo.getDescription());
            existingCategory.setMiniPhoto(categoryInfo.getMiniPhoto());
            existingCategory.setMaxiPhoto(categoryInfo.getMaxiPhoto());

            existingCategory.persist();
            return existingCategory;
        } else {
            return null;
        }
    }

    // Delete CategoryInfo
    public boolean deleteCategoryInfo(String id) {
        return CategoryInfo.deleteById(new ObjectId(id));
    }
}