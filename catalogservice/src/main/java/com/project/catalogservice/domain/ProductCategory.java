package com.project.catalogservice.domain;

public enum ProductCategory {
    ELECTRONICS,
    CLOTHING,
    TOYS,
    BOOKS,
    SPORTS_EQUIPMENT;

    public static boolean contains(String category) {
        for (ProductCategory pc : ProductCategory.values()) {
            if (pc.name().equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }
}
