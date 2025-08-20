package com.shopping.exception;

/**
 * 권한이 없을 때 발생하는 예외
 */
public class UnauthorizedException extends Exception {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}