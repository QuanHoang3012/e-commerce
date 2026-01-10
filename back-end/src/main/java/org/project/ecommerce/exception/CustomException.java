package org.project.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final int code;
    private final HttpStatus status;

    public CustomException(String message, int code) {
        super(message);
        this.code = code;
        this.status = HttpStatus.valueOf(code);
    }
}
