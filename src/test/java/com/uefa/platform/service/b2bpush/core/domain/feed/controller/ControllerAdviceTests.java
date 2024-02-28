package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.dto.common.ErrorResponse;
import com.uefa.platform.service.b2bpush.core.domain.exception.InvalidDtoFieldsException;
import com.uefa.platform.service.b2bpush.core.domain.exception.TooManyRequestedResultsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

@SpringBootTest
class ControllerAdviceTests {

    @Autowired
    ControllerAdvice controllerAdvice;

    static List<Arguments> bindingResultsArguments() {
        return List.of(
                Arguments.of(bindingResultWithErrors(
                        fieldErrorGenerator("code",
                                "code must be with valid Regex"),
                        fieldErrorGenerator("title", "title cannot be null"),
                        fieldErrorGenerator("type", "type must not be null")
                )),
                Arguments.of(bindingResultWithErrors(
                        fieldErrorGenerator("number",
                                "number must be integer"),
                        fieldErrorGenerator("feed", "feed must have a type")
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("bindingResultsArguments")
    void testInvalidDtoExceptionHandling(BindingResult bindingResult) {
        ResponseEntity<Object> response =
                controllerAdvice.handleBadRequest(
                        new InvalidDtoFieldsException(bindingResult)
                );
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        // All the BindingResults field error messages are contained in the ErrorResponse message
        Assertions.assertTrue(
                bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .allMatch(errorMsg -> errorResponse.getError().getMessage().contains(errorMsg))
        );
        // ErrorResponse has the expected HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),
                errorResponse.getError().getStatus());
    }

    @Test
    void testTooManyRequestedResultsException() {
        final String parameterName = "limit";
        final int limit = 50;

        ResponseEntity<Object> response =
                controllerAdvice.handleBadRequest(new TooManyRequestedResultsException(parameterName, limit));

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        String errorMessage = errorResponse.getError().getMessage();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getError().getStatus());
        Assertions.assertTrue(errorMessage.contains(String.valueOf(limit)));
        Assertions.assertTrue(errorMessage.contains(parameterName));
    }

    private static BindingResult bindingResultWithErrors(FieldError... fieldErrors) {
        BindingResult bindingResult = mock(BindingResult.class);

        Mockito.when(bindingResult.getFieldErrors())
                .thenReturn(Stream.of(fieldErrors).toList());

        return bindingResult;
    }

    private static FieldError fieldErrorGenerator(String field,
                                                  String defaultMessage) {

        return new FieldError(field, field, defaultMessage);
    }

}
