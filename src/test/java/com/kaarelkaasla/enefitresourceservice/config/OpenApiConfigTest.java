package com.kaarelkaasla.enefitresourceservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

class OpenApiConfigTest {

  private final OpenApiConfig openApiConfig = new OpenApiConfig();

  @Test
  void customOpenAPI_ShouldCreateOpenAPIWithCurrentServerUrl() {
    OpenAPI result = openApiConfig.customOpenAPI();

    assertThat(result).isNotNull();
    assertThat(result.getServers()).hasSize(1);

    Server server = result.getServers().get(0);
    assertThat(server.getUrl()).isEqualTo("/");
    assertThat(server.getDescription()).isEqualTo("Current server");
  }
}
