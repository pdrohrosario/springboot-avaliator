package com.project.catalogservice.product.infrastruct.input.output.repository;

import com.project.catalogservice.product.infrastruct.input.output.entities.JpaProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaProductRepository extends JpaRepository<JpaProduct, String> {
    Optional<JpaProduct> findByName(String name);

    Page<JpaProduct> findByNameAndDescription(String name, String description, Pageable pageable);
}
