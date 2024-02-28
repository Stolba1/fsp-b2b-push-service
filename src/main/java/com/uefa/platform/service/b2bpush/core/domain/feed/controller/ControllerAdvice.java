package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.dto.common.ErrorResponse;
import com.uefa.platform.service.b2bpush.core.domain.exception.InvalidDtoFieldsException;
import com.uefa.platform.service.b2bpush.core.domain.exception.TooManyRequestedResultsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler({FeedWithCodeAlreadyExistsException.class,
            ClientWithRoutingKeyAlreadyExistsException.class,
            ClientWithNameAlreadyExistsException.class,
            InvalidDtoFieldsException.class,
            TooManyRequestedResultsException.class
    })
    protected ResponseEntity<Object> handleBadRequest(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    public static class ClientWithRoutingKeyAlreadyExistsException extends RuntimeException {

        public ClientWithRoutingKeyAlreadyExistsException(String routingKey) {
            super(String.format("Client with routingKey: %s found in repository", routingKey));
        }
    }

    public static class FeedWithCodeAlreadyExistsException extends RuntimeException {

        public FeedWithCodeAlreadyExistsException(String code) {
            super(String.format("Feed with code: %s found in repository", code));
        }
    }

    public static class ClientWithNameAlreadyExistsException extends RuntimeException {

        public ClientWithNameAlreadyExistsException(String name) {
            super(String.format("Client with name: '%s' already found in repository", name));
        }

    }
}
