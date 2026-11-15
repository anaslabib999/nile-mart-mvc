package org.route.service;

import org.route.dto.CategoryRow;
import org.route.model.Category;
import org.route.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {
    private static final Comparator<Category> BY_ORDER =
            Comparator.comparing(Category::getDisplayOrder).thenComparing(Category::getName);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /** A category is top-level when its parentSlug is empty (CAT-01). */
    public boolean isTopLevel(Category category) {
        return category.getParentSlug() == null || category.getParentSlug().isBlank();
    }

    public List<Category> getTopLevelCategories() {
        List<Category> result = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            if (isTopLevel(category)) {
                result.add(category);
            }
        }
        result.sort(BY_ORDER);
        return result;
    }

    public List<Category> getSubCategories() {
        List<Category> result = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            if (!isTopLevel(category)) {
                result.add(category);
            }
        }
        result.sort(BY_ORDER);
        return result;
    }

    public List<Category> getSubCategoriesOf(String parentSlug) {
        List<Category> result = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            if (parentSlug.equals(category.getParentSlug())) {
                result.add(category);
            }
        }
        result.sort(BY_ORDER);
        return result;
    }

    /** True when the slug belongs to an existing sub-category (the only place products may go). */
    public boolean isSubCategory(String slug) {
        Category category = categoryRepository.findBySlug(slug);
        return category != null && !isTopLevel(category);
    }

    /** Every top-level category followed by its own sub-categories, both sorted by display order. */
    public List<CategoryRow> getCategoryTree() {
        List<CategoryRow> rows = new ArrayList<>();
        for (Category top : getTopLevelCategories()) {
            rows.add(new CategoryRow(top.getName(), top.getSlug(), "—",
                    top.getDisplayOrder(), top.isActive(), false));
            for (Category sub : getSubCategoriesOf(top.getSlug())) {
                rows.add(new CategoryRow(sub.getName(), sub.getSlug(), top.getName(),
                        sub.getDisplayOrder(), sub.isActive(), true));
            }
        }
        return rows;
    }

    /** Returns the business-rule errors as field name -> message (empty when the category is valid). */
    public Map<String, String> validate(Category category) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (category.getSlug() != null && categoryRepository.findBySlug(category.getSlug()) != null) {
            errors.put("slug", "Slug already exists");
        }

        String parentSlug = normalize(category.getParentSlug());
        Category parent = null;
        boolean parentOk = true;
        if (!parentSlug.isEmpty()) {
            parent = categoryRepository.findBySlug(parentSlug);
            if (parent == null) {
                errors.put("parentSlug", "Parent category does not exist");
                parentOk = false;
            } else if (!isTopLevel(parent)) {
                errors.put("parentSlug", "Parent must be a top-level category");
                parentOk = false;
            }
        }

        if (parentOk && category.getName() != null && isNameTakenUnder(category.getName().trim(), parentSlug)) {
            String where = parent == null ? "at the top level" : "under " + parent.getName();
            errors.put("name", "A category with this name already exists " + where);
        }
        return errors;
    }

    public void saveCategory(Category category) {
        category.setName(category.getName().trim());
        category.setParentSlug(normalize(category.getParentSlug()));
        categoryRepository.save(category);
    }

    private boolean isNameTakenUnder(String name, String parentSlug) {
        for (Category existing : categoryRepository.findAll()) {
            if (normalize(existing.getParentSlug()).equals(parentSlug)
                    && existing.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String parentSlug) {
        return parentSlug == null ? "" : parentSlug.trim();
    }
}
