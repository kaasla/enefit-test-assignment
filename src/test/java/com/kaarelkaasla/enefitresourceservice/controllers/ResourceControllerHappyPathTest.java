package com.kaarelkaasla.enefitresourceservice.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarelkaasla.enefitresourceservice.dtos.*;
import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;
import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;
import com.kaarelkaasla.enefitresourceservice.services.ResourceService;
import com.kaarelkaasla.enefitresourceservice.services.TimeProvider;

@WebMvcTest(ResourceController.class)
class ResourceControllerHappyPathTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ResourceService resourceService;
  @MockitoBean private TimeProvider timeProvider;

  @BeforeEach
  void setup() {
    when(timeProvider.now()).thenReturn(OffsetDateTime.parse("2024-01-01T00:00:00Z"));
  }

  private ResourceResponse sampleResponse(long id, ResourceType type, String cc) {
    return new ResourceResponse(
        id,
        type,
        cc,
        1L,
        OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        new LocationResponse(id, "123 Test St", "City", "12345", cc),
        Set.of(
            new CharacteristicResponse(id, "TEST1", CharacteristicType.CONSUMPTION_TYPE, "RES")));
  }

  @Test
  void createResource_Returns201WithPayload() throws Exception {
    ResourceRequest req =
        new ResourceRequest(
            ResourceType.METERING_POINT,
            "US",
            new LocationRequest("123 Test St", "City", "12345", "US"),
            Set.of(new CharacteristicRequest("TEST1", CharacteristicType.CONSUMPTION_TYPE, "RES")));

    when(resourceService.createResource(any(ResourceRequest.class)))
        .thenReturn(sampleResponse(1L, ResourceType.METERING_POINT, "US"));

    mockMvc
        .perform(
            post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.type").value("METERING_POINT"))
        .andExpect(jsonPath("$.countryCode").value("US"))
        .andExpect(jsonPath("$.location.postalCode").value("12345"));
  }

  @Test
  void getAllResources_ReturnsArray() throws Exception {
    when(resourceService.getAllResources())
        .thenReturn(
            List.of(
                sampleResponse(1L, ResourceType.METERING_POINT, "US"),
                sampleResponse(2L, ResourceType.CONNECTION_POINT, "DE")));

    mockMvc
        .perform(get("/api/v1/resources"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].type").value("METERING_POINT"))
        .andExpect(jsonPath("$[1].type").value("CONNECTION_POINT"));
  }

  @Test
  void updateResource_ReturnsUpdatedPayload() throws Exception {
    ResourceRequest req =
        new ResourceRequest(
            ResourceType.CONNECTION_POINT,
            "DE",
            new LocationRequest("456 Updated Ave", "Berlin", "10115", "DE"),
            Set.of(
                new CharacteristicRequest(
                    "UPD1", CharacteristicType.CONNECTION_POINT_STATUS, "ACTIVE")));

    when(resourceService.updateResource(eq(10L), any(ResourceRequest.class)))
        .thenReturn(sampleResponse(10L, ResourceType.CONNECTION_POINT, "DE"));

    mockMvc
        .perform(
            put("/api/v1/resources/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.type").value("CONNECTION_POINT"))
        .andExpect(jsonPath("$.countryCode").value("DE"));
  }

  @Test
  void patchResource_ReturnsPatchedPayload() throws Exception {
    PatchResourceRequest patch =
        new PatchResourceRequest(
            java.util.Optional.of(ResourceType.CONNECTION_POINT),
            java.util.Optional.of("DE"),
            java.util.Optional.empty(),
            java.util.Optional.empty());

    when(resourceService.patchResource(eq(5L), any(PatchResourceRequest.class)))
        .thenReturn(sampleResponse(5L, ResourceType.CONNECTION_POINT, "DE"));

    mockMvc
        .perform(
            patch("/api/v1/resources/{id}", 5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.type").value("CONNECTION_POINT"))
        .andExpect(jsonPath("$.countryCode").value("DE"));
  }

  @Test
  void deleteResource_Returns204() throws Exception {
    mockMvc.perform(delete("/api/v1/resources/{id}", 42)).andExpect(status().isNoContent());
    verify(resourceService).deleteResource(42L);
  }

  @Test
  void batchNotification_ReturnsSummary() throws Exception {
    BatchNotificationResponse response =
        new BatchNotificationResponse(
            java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            3,
            "COMPLETED",
            OffsetDateTime.parse("2024-01-01T00:00:00Z"),
            ResourceEventType.BATCH_NOTIFICATION);
    when(resourceService.notifyAllResources()).thenReturn(response);

    mockMvc
        .perform(post("/api/v1/resources/send-all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operationId").value("123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(jsonPath("$.resourceCount").value(3))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.operation").value("BATCH_NOTIFICATION"))
        .andExpect(jsonPath("$.processedAt").exists());
  }
}
