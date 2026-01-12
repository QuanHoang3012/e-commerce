package org.project.ecommerce.base;

import org.project.ecommerce.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected <T> BaseResponse<T> wrapSuccess(T data){
        return BaseResponse.success(data);
    }
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex){
        return ResponseEntity.status(ex.getStatus())
                .body(BaseResponse.failure(ex.getMessage(), ex.getStatus().value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.failure(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegalStateException(IllegalStateException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.failure(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<BaseResponse<Object>> handleNullPointerException(NullPointerException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.failure(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

//    @ExceptionHandler(AuthorizationDeniedException.class)
//    public ResponseEntity<BaseResponse<Object>> handleAuthorizationException(AuthorizationDeniedException ex){
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResponse.failure(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
        logger.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        BaseResponse.failure(
                                ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
