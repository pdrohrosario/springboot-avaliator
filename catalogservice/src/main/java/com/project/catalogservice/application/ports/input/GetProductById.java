package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.application.ports.output.GetProductOutput;

public interface GetProductById {
    GetProductOutput execute(Long id);
}
