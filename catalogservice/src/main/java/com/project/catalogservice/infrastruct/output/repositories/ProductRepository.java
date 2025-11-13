package com.project.catalogservice.infrastruct.output.repositories;

import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.output.entities.JpaProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<JpaProduct, Long> {
    Optional<JpaProduct> findByName(String name);
}
