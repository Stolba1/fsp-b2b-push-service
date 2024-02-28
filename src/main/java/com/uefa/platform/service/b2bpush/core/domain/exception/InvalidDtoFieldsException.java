package com.uefa.platform.service.b2bpush.core.domain.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception to be thrown when DTO is received with invalid Fields
 */
public class InvalidDtoFieldsException extends RuntimeException {

    private final Set<String> invalidFields;

    public InvalidDtoFieldsException(BindingResult bindingResult) {
        this.invalidFields = extractInvalidFields(bindingResult);
    }

    private Set<String> extractInvalidFields(BindingResult bindingResult) {
        // If there are validation annotations at Class level,
        // then correct method would be "getAllErrors()"
        return bindingResult.getFieldErrors().stream()
                .map(this::extractApplicableMessage)
                .collect(Collectors.toSet());
    }

    @Override
    public String getMessage() {
        return String.join(" && ", invalidFields);
    }

    private String extractApplicableMessage(FieldError fieldError) {
        return Objects.nonNull(fieldError.getDefaultMessage()) ?
                fieldError.getDefaultMessage()
                : fieldError.getObjectName();
    }

    public Set<String> getInvalidFields() {
        return invalidFields;
    }

}
