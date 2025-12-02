package com.project.catalogservice.application.ports.useCases;

import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.output.FindById;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class GetProductByIdUseCase implements GetProductById {

    private final FindById find;

    public GetProductByIdUseCase(FindById find) {
        this.find = find;
    }

    @Override
    public ProductResponse execute(Long id) {
        return ProductResponse.fromDomain(find.execute(id));
    }
}
