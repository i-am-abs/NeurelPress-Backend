package com.neurelpress.blogs.exception;

/**
 * Thrown when outbound email (e.g. OTP) cannot be delivered so the API can return a clear error.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
