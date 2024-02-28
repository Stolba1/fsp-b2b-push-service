package com.uefa.platform.service.b2bpush.core.domain.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class TooManyRequestedResultsExceptionTests {

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("limit", 90),
                Arguments.of("limit", 200)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testThrowException(String parameterName, int limit) {
        TooManyRequestedResultsException exception = Assertions.assertThrows(
                TooManyRequestedResultsException.class,
                () -> {
                    throw new TooManyRequestedResultsException(parameterName, limit);
                }
        );

        String exceptionMessage = exception.getMessage();

        Assertions.assertTrue(exceptionMessage.contains(String.valueOf(limit)));
        Assertions.assertTrue(exceptionMessage.contains(parameterName));
    }

}
