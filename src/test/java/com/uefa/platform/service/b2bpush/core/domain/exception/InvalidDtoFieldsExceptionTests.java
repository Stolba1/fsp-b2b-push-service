package com.uefa.platform.service.b2bpush.core.domain.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;

class InvalidDtoFieldsExceptionTests {

    @Test
    void testThrowException() {
        BindingResult bindingResult = mock(BindingResult.class);

        Mockito.when(bindingResult.getFieldErrors()).thenReturn(
                List.of(
                        new FieldError("data-1", "field", "data-1 must be valid"),
                        new FieldError("data-2", "field", "data-2 must be valid"),
                        new FieldError("data-3", "field", null, false,
                                null, null, null )
                )
        );

        InvalidDtoFieldsException exception = Assertions.assertThrows(
                InvalidDtoFieldsException.class,
                () -> {
                    throw new InvalidDtoFieldsException(bindingResult);
                }
        );

        Assertions.assertEquals(Set.of("data-1 must be valid", "data-2 must be valid", "data-3"),
                exception.getInvalidFields());
        Assertions.assertTrue(
                exception.getInvalidFields().stream()
                        .allMatch(invalidField -> exception.getMessage().contains(invalidField))
        );
    }

}
