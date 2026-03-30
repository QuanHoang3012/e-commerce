package org.project.ecommerce.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    private Instant timestamp;
    private String path;
    private boolean success;
    private int code;
    private String message;
    private T data;
    private Map<String, String> errors;

    private static String getCurrentPath(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes){
            return servletRequestAttributes.getRequest().getRequestURI();
        }
        return "/";
    }

    public static <T> BaseResponse<T> success(T data){
        return BaseResponse.<T>builder()
                .timestamp(Instant.now())
                .path(getCurrentPath())
                .success(true)
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> failure(String message, int code) {
        return failure(message, code, null);
    }

    public static <T> BaseResponse<T> failure(String message, int code, Map<String, String> errors) {
        return BaseResponse.<T>builder()
                .timestamp(Instant.now())
                .path(getCurrentPath())
                .success(false)
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }
}
