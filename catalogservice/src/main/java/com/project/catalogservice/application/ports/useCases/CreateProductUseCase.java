package com.project.catalogservice.application.ports.useCases;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.application.ports.output.FindProductByName;
import com.project.catalogservice.application.ports.output.SaveProduct;
import com.project.catalogservice.domain.ProductAlreadyExistsException;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import org.springframework.stereotype.Service;

import com.project.catalogservice.domain.Product;

@Service
public class CreateProductUseCase implements CreateProduct {

    private final SaveProduct save;

    private final FindProductByName findByName;

    public CreateProductUseCase(SaveProduct save, FindProductByName findByName) {
        this.save = save;
        this.findByName = findByName;
    }

    @Override
    public Product execute(ProductRequest request) {
        if(null != findByName.execute(request.name())){
            throw new ProductAlreadyExistsException(request.name());
        };
        Product newProduct = Product.create(request.name(), request.price(), request.description(), request.category());
        return save.execute(newProduct);
    }
}
