package com.project.catalogservice.infrastruct.product.input.output.repositories;

import com.project.catalogservice.infrastruct.product.input.output.entities.JpaProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<JpaProduct, Long> {
    Optional<JpaProduct> findByName(String name);

    Page<JpaProduct> findByNameAndDescription(String name, String description, Pageable pageable);
}
