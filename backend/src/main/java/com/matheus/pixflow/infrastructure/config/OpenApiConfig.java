package com.matheus.pixflow.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PixFlow: Event-Driven Payment & Settlement Engine API")
                        .version("1.0.0")
                        .description("High-performance transactional engine for real-time PIX charge generation, asynchronous clearing via AWS SQS / Workers, and state-machine settlement.")
                        .contact(new Contact()
                                .name("Matheus Araujo Pereira")
                                .email("matharaupere@gmail.com")
                                .url("https://github.com/matheus-araujo-pereira/pix-flow"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("/").description("Current Server Context"),
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}

