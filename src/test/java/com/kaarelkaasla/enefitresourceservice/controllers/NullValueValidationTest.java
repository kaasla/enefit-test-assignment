package com.kaarelkaasla.enefitresourceservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kaarelkaasla.enefitresourceservice.exceptions.ResourceNotFoundException;
import com.kaarelkaasla.enefitresourceservice.services.ResourceService;
import com.kaarelkaasla.enefitresourceservice.services.TimeProvider;

@WebMvcTest(ResourceController.class)
class NullValueValidationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ResourceService resourceService;

  @MockitoBean private TimeProvider timeProvider;

  @BeforeEach
  void setUp() {
    when(timeProvider.now()).thenReturn(OffsetDateTime.now());
  }

  @Test
  void createResource_NullType_Returns400() throws Exception {
    String requestJson =
        """
        {
            "type": null,
            "countryCode": "US",
            "location": {
                "streetAddress": "123 Main St",
                "city": "City",
                "postalCode": "12345",
                "countryCode": "US"
            },
            "characteristics": []
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/resources").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void createResource_NullCountryCode_Returns400() throws Exception {
    String requestJson =
        """
        {
            "type": "METERING_POINT",
            "countryCode": null,
            "location": {
                "streetAddress": "123 Main St",
                "city": "City",
                "postalCode": "12345",
                "countryCode": "US"
            },
            "characteristics": []
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/resources").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void createResource_NullLocationFields_Returns400() throws Exception {
    String requestJson =
        """
        {
            "type": "METERING_POINT",
            "countryCode": "US",
            "location": {
                "streetAddress": null,
                "city": null,
                "postalCode": null,
                "countryCode": null
            },
            "characteristics": []
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/resources").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void createResource_NullCharacteristicFields_Returns400() throws Exception {
    String requestJson =
        """
        {
            "type": "METERING_POINT",
            "countryCode": "US",
            "location": {
                "streetAddress": "123 Main St",
                "city": "City",
                "postalCode": "12345",
                "countryCode": "US"
            },
            "characteristics": [
                {
                    "code": null,
                    "type": null,
                    "value": null
                }
            ]
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/resources").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void patchResource_NullRequiredFields_Ignored_Returns404() throws Exception {

    String patchJson =
        """
        {
            "type": null,
            "countryCode": null
        }
        """;

    when(resourceService.patchResource(eq(1L), any()))
        .thenThrow(new ResourceNotFoundException("Resource with ID 1 not found"));

    mockMvc
        .perform(
            patch("/api/v1/resources/1").contentType(MediaType.APPLICATION_JSON).content(patchJson))
        .andExpect(status().isNotFound());
  }

  @Test
  void patchResource_NullOptionalFields_Returns404() throws Exception {
    String patchJson =
        """
        {
            "location": null,
            "characteristics": null
        }
        """;

    when(resourceService.patchResource(eq(1L), any()))
        .thenThrow(
            new com.kaarelkaasla.enefitresourceservice.exceptions.ResourceNotFoundException(
                "Resource with ID 1 not found"));

    mockMvc
        .perform(
            patch("/api/v1/resources/1").contentType(MediaType.APPLICATION_JSON).content(patchJson))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateResource_NullRequiredFields_Returns400() throws Exception {
    String requestJson =
        """
        {
            "type": null,
            "countryCode": null,
            "location": {
                "streetAddress": "123 Main St",
                "city": "City",
                "postalCode": "12345",
                "countryCode": "US"
            },
            "characteristics": []
        }
        """;

    mockMvc
        .perform(
            put("/api/v1/resources/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void createResource_EmptyRequiredFields_Returns400() throws Exception {
    String requestJson =
        """
        {
            "type": "METERING_POINT",
            "countryCode": "",
            "location": {
                "streetAddress": "",
                "city": "",
                "postalCode": "",
                "countryCode": ""
            },
            "characteristics": [
                {
                    "code": "",
                    "type": "CONSUMPTION_TYPE",
                    "value": ""
                }
            ]
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/resources").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }
}
