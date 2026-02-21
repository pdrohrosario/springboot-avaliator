package com.project.feedbackservice.review.config.exception;

public class ApiIntegrationException extends RuntimeException {
    public ApiIntegrationException(){
        super("Erro on integration with API Catalog.Verify if it is available");
    }
}
