package com.mc.mc_server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "MC Server API",
        description = "MC Server REST API 문서",
        version = "1.0.0",
        contact = @Contact(
            name = "MC Server Team",
            email = "mc-server@example.com"
        )
    )
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT 토큰을 입력하세요 (Bearer 접두사 제외)"
)
public class SwaggerConfig {
    // Spring Boot 3.x에서는 @OpenAPIDefinition과 @SecurityScheme 어노테이션으로 설정
}
