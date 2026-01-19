package org.project.ecommerce.config.authentication;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor để validate API Key cho các endpoint được đánh dấu @RequireApiKey
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {
    
    @Value("${admin.api.key:admin-secret-key-123}")
    private String adminApiKey;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireApiKey annotation = handlerMethod.getMethodAnnotation(RequireApiKey.class);
            
            // Nếu method có @RequireApiKey, kiểm tra API key
            if (annotation != null) {
                String apiKey = request.getHeader("X-Admin-Key");
                
                if (apiKey == null || !apiKey.equals(adminApiKey)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Không có quyền truy cập");
                    errorResponse.put("error", "UNAUTHORIZED");
                    
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    return false;
                }
            }
        }
        
        return true;
    }
}
