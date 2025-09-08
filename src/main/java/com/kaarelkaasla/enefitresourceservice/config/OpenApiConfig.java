package com.kaarelkaasla.enefitresourceservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configures OpenAPI metadata and server list for Swagger UI.
 * Adds a single server pointing to the current base path.
 */
@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Resource Management API",
            description =
                "API for managing metering points and connection points with location data and"
                    + " characteristics",
            version = "v1.0.0"))
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().servers(List.of(new Server().url("/").description("Current server")));
  }
}
