package org.route.service;

import org.route.model.Brand;
import org.route.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandService {
    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<Brand> getActiveBrands() {
        List<Brand> activeBrands = new ArrayList<>();
        for (Brand brand : brandRepository.findAll()) {
            if (brand.isActive()) {
                activeBrands.add(brand);
            }
        }
        return activeBrands;
    }

    public Brand findBySlug(String slug) {
        return brandRepository.findBySlug(slug);
    }

    /** Returns the business-rule errors as field name -> message (empty when the brand is valid). */
    public Map<String, String> validate(Brand brand) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (brand.getName() != null && isNameTaken(brand.getName().trim())) {
            errors.put("name", "Brand name already exists");
        }
        if (brand.getSlug() != null && brandRepository.findBySlug(brand.getSlug()) != null) {
            errors.put("slug", "Slug already exists");
        }
        return errors;
    }

    public void saveBrand(Brand brand) {
        brand.setName(brand.getName().trim());
        brandRepository.save(brand);
    }

    private boolean isNameTaken(String name) {
        for (Brand existing : brandRepository.findAll()) {
            if (existing.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
