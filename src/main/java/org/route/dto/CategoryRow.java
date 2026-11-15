package org.route.dto;

/** One line of the categories list page. */
public record CategoryRow(String name, String slug, String parentName,
                          Integer displayOrder, boolean active, boolean subCategory) {
}
