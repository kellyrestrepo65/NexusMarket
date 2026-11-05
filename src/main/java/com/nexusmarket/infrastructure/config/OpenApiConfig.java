package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.models.user.User;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** API documentation served at /swagger-ui.html (contract at /v3/api-docs). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusMarketOpenApi() {
        return new OpenAPI().info(new Info()
                .title("NexusMarket API")
                .version("1.0.0")
                .description("Marketplace API. Every operation is executed by the user sent in the X-User-Id header "
                        + "(RG-01); buyer registration is the only public operation."));
    }
}
