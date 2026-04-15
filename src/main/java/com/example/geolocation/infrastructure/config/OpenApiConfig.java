package com.example.geolocation.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido via POST /auth/login"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IP Geolocation Service API")
                        .version("1.0.0")
                        .description("""
                            Microserviço para identificação geográfica a partir de endereços IP.

                            ## Autenticação

                            1. Faça login em `POST /auth/login` com username e password
                            2. Copie o token JWT retornado
                            3. Clique no botão **Authorize** 🔒 e cole o token

                            **Credenciais de teste:** `admin` / `Admin123@`
                            """)
                        .contact(new Contact()
                                .name("Rafael Souza")
                                .email("rafael@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

