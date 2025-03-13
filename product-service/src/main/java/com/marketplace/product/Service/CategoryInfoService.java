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

    // Inject the custom CategoryInfoRepository
    @Inject
    CategoryInfoRepository categoryInfoRepository;

    // Create or Update CategoryInfo
    public CategoryInfo createOrUpdateCategoryInfo(CategoryInfo categoryInfo) {
        if (categoryInfo.id == null) {
            categoryInfo.persist();
        } else {
            CategoryInfo existingCategory = categoryInfoRepository.findById(categoryInfo.id);
            if (existingCategory != null) {
                existingCategory.name = categoryInfo.name;
                existingCategory.description = categoryInfo.description;
                categoryInfoRepository.persist(existingCategory); // Save updated entity
            }
        }
        return categoryInfo;
    }

    // Find CategoryInfo by ID
    public Optional<CategoryInfo> getCategoryInfoById(String id) {
        return Optional.ofNullable(categoryInfoRepository.findById(new ObjectId(id)));
    }

    // Get all CategoryInfo
    public List<CategoryInfo> getAllCategoryInfo() {
        return categoryInfoRepository.listAll();
    }

    // Delete CategoryInfo by ID
    public boolean deleteCategoryInfo(String id) {
        CategoryInfo categoryInfo = categoryInfoRepository.findById(new ObjectId(id));
        if (categoryInfo != null) {
            categoryInfoRepository.delete(categoryInfo);
            return true;
        }
        return false;
    }
}