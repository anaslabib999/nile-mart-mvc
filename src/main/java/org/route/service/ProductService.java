package org.route.service;


import org.route.dto.ProductRow;
import org.route.model.Brand;
import org.route.model.Category;
import org.route.model.Product;
import org.route.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          BrandService brandService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    public List<Product> getAvailableProducts() {
        List<Product> availableProducts = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p.getInStock() > 0)
                availableProducts.add(p);
        }
        return availableProducts;
    }

    /** Available products with the category name and brand name looked up ("—" when no brand). */
    public List<ProductRow> getAvailableProductRows() {
        List<ProductRow> rows = new ArrayList<>();
        for (Product p : getAvailableProducts()) {
            Category category = categoryService.findBySlug(p.getCategory());
            String categoryName = category != null ? category.getName() : p.getCategory();

            String brandName = "—";
            if (p.getBrandSlug() != null && !p.getBrandSlug().isBlank()) {
                Brand brand = brandService.findBySlug(p.getBrandSlug());
                brandName = brand != null ? brand.getName() : p.getBrandSlug();
            }
            rows.add(new ProductRow(p.getSku(), p.getName(), p.getPrice(), p.getInStock(),
                    categoryName, brandName));
        }
        return rows;
    }

    /** Returns the business-rule errors as field name -> message (empty when the product is valid). */
    public Map<String, String> validate(Product product) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (product.getSku() != null && isProductExist(product.getSku())) {
            errors.put("sku", "sku is already exist");
        }

        String category = product.getCategory();
        if (category != null && !category.isBlank() && !categoryService.isSubCategory(category)) {
            errors.put("category", "Category must be an existing sub-category");
        }

        String brandSlug = product.getBrandSlug();
        if (brandSlug != null && !brandSlug.isBlank()) {
            Brand brand = brandService.findBySlug(brandSlug);
            if (brand == null) {
                errors.put("brandSlug", "Brand does not exist");
            } else if (!brand.isActive()) {
                errors.put("brandSlug", "Brand is not active");
            }
        }
        return errors;
    }

    public void saveProduct(Product product) {
        if (product.getBrandSlug() != null && product.getBrandSlug().isBlank()) {
            product.setBrandSlug(null);
        }
        productRepository.save(product);
    }

    public boolean isProductExist(String sku) {
        return productRepository.findBySku(sku) != null;
    }
}
