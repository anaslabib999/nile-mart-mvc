package org.route.dto;

import java.math.BigDecimal;

/** One line of the products list page, with category and brand names already looked up. */
public record ProductRow(String sku, String name, BigDecimal price, Integer inStock,
                         String categoryName, String brandName) {
}
