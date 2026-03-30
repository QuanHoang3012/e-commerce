package org.project.ecommerce.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;

/**
 * Cấu hình Swagger/OpenAPI 3.0
 * Swagger UI: http://localhost:8080/swagger-ui/index.html
 * API Docs: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI eCommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API Documentation")
                        .description("API documentation cho hệ thống E-Commerce với các chức năng: " +
                                "Quản lý sản phẩm, Giỏ hàng, Checkout, Thanh toán, Tracking đơn hàng")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("E-Commerce Development Team")
                                .email("support@ecommerce.com")
                                .url("https://ecommerce.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                // JWT Bearer security scheme
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập access token JWT vào đây. Ví dụ: eyJhbGci...")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
