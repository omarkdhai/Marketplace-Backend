package com.marketplace.product.Service;

import com.marketplace.product.Entity.CategoryInfo;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoryInfoService {

    // Create or Update CategoryInfo
    public CategoryInfo createOrUpdateCategoryInfo(CategoryInfo categoryInfo) {
        if (categoryInfo.id == null) {
            categoryInfo.persist();
        } else {
            CategoryInfo existingCategory = CategoryInfo.findById(categoryInfo.id);
            if (existingCategory != null) {
                existingCategory.name = categoryInfo.name;
                existingCategory.description = categoryInfo.description;
                existingCategory.persist();
            }
        }
        return categoryInfo;
    }

    // Find CategoryInfo by ID
    public Optional<CategoryInfo> getCategoryInfoById(String id) {
        return Optional.ofNullable(CategoryInfo.findById(new ObjectId(id)));
    }

    // Get all CategoryInfo
    public List<CategoryInfo> getAllCategoryInfo() {
        return CategoryInfo.listAll();
    }

    // Delete CategoryInfo by ID
    public boolean deleteCategoryInfo(String id) {
        CategoryInfo categoryInfo = CategoryInfo.findById(new ObjectId(id));
        if (categoryInfo != null) {
            categoryInfo.delete();
            return true;
        }
        return false;
    }
}