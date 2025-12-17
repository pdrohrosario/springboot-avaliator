package com.project.catalogservice.product.infrastruct.input.output.repository;

import com.project.catalogservice.product.infrastruct.input.output.entities.JpaProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaProductRepository extends JpaRepository<JpaProduct, UUID> {
    Optional<JpaProduct> findByName(String name);

    @Query("""
       SELECT p FROM JpaProduct p
       WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
         AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')))
    """)
    Page<JpaProduct> searchByNameAndDescription(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);
}
