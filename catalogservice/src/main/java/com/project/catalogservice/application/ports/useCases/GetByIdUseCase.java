package com.project.catalogservice.application.ports.useCases;

import com.project.catalogservice.application.ports.input.GetById;
import com.project.catalogservice.application.ports.output.FindById;
import com.project.catalogservice.domain.Product;
import org.springframework.stereotype.Service;

@Service
public class GetByIdUseCase implements GetById {

    private final FindById find;

    public GetByIdUseCase(FindById find) {
        this.find = find;
    }

    @Override
    public Product execute(Long id) {
        return find.execute(id);
    }
}
