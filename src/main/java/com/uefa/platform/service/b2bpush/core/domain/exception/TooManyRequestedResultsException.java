package com.uefa.platform.service.b2bpush.core.domain.exception;

public class TooManyRequestedResultsException extends RuntimeException {

    public TooManyRequestedResultsException(String parameterName, int maxResults) {
        super(String.format("Too many requested results with parameter '%s'." +
                " Maximum results to be queried with this parameter is: %d", parameterName, maxResults));
    }

}
